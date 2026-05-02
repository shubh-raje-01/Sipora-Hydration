package in.sipora.backend.modules.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.sipora.backend.modules.ordering.api.OrderSummary;
import in.sipora.backend.modules.ordering.api.OrderingModuleApi;
import in.sipora.backend.modules.payment.domain.Payment;
import in.sipora.backend.modules.payment.domain.PaymentRepository;
import in.sipora.backend.modules.payment.domain.PaymentStatus;
import in.sipora.backend.modules.payment.infrastructure.GatewayOrderResult;
import in.sipora.backend.modules.payment.infrastructure.PaymentGateway;
import in.sipora.backend.modules.payment.web.PaymentDTOs.CreatePaymentOrderRequest;
import in.sipora.backend.modules.payment.web.PaymentDTOs.CreatePaymentOrderResponse;
import in.sipora.backend.modules.payment.web.PaymentDTOs.PaymentResponse;
import in.sipora.backend.modules.payment.web.PaymentDTOs.VerifyPaymentRequest;
import in.sipora.backend.modules.payment.web.PaymentDTOs.VerifyPaymentResponse;
import in.sipora.backend.modules.payment.web.PaymentDTOs.WebhookEvent;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core payment service.
 *
 * FULL RAZORPAY PAYMENT FLOW:
 *
 * 1. POST /api/v1/orders (ordering module)
 *    → order saved, status = PENDING_PAYMENT
 *    → returns orderId to frontend
 *
 * 2. POST /api/v1/payments/create-order  (this module)
 *    → calls Razorpay API to create a gateway order
 *    → saves Payment record (status=PENDING)
 *    → calls orderingModuleApi.setGatewayOrderId()
 *    → returns razorpayOrderId + keyId to frontend
 *
 * 3. Frontend opens Razorpay checkout modal using those details.
 *    Customer pays → Razorpay calls our webhook AND returns
 *    razorpay_payment_id + razorpay_signature to the frontend callback.
 *
 * 4a. POST /api/v1/payments/webhook  (Razorpay → us, server-to-server)
 *    → verify HMAC signature
 *    → handle payment.captured → confirm order, update payment
 *    → handle payment.failed → cancel order, update payment
 *
 * 4b. POST /api/v1/payments/verify (frontend → us, client-side confirmation)
 *    → verify frontend signature
 *    → returns current payment status to frontend
 *    → frontend shows success/failure screen
 *
 * Steps 4a and 4b may arrive in any order. Both are idempotent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final OrderingModuleApi orderingModuleApi;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;

    private static final String EVENT_CAPTURED = "payment.captured";
    private static final String EVENT_FAILED = "payment.failed";

    // ── Step 2: Create gateway order ==>

    /**
     * Creates a Razorpay order for the given internal orderId.
     *
     * Idempotent — if a PENDING payment already exists for this order's
     * gateway order ID, the existing record is returned without calling
     * Razorpay again. This handles frontend retry on network error.
     */
    @Transactional
    public CreatePaymentOrderResponse createPaymentOrder(UUID userId,
                                                         CreatePaymentOrderRequest request) {
        OrderSummary order = orderingModuleApi.getOrderById(request.orderId())
                .orElseThrow(() -> ResourceNotFoundException.of("Order", request.orderId()));

        // Only the order owner can initiate payment
        if (!order.userId().equals(userId)) {
            throw new DomainException("Order not found", HttpStatus.NOT_FOUND);
        }

        if (!order.status().equals("PENDING_PAYMENT")) {
            throw new DomainException(
                    "Payment can only be initiated for orders in PENDING_PAYMENT status. " +
                            "Current status: " + order.status(),
                    HttpStatus.CONFLICT);
        }

        // Idempotency — if a gateway order already exists, reuse it
        if (order.gatewayOrderId() != null) {
            return paymentRepository.findByGatewayOrderId(order.gatewayOrderId())
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .map(existing -> buildCreateResponse(existing, order))
                    .orElseGet(() -> createNewGatewayOrder(order));
        }

        return createNewGatewayOrder(order);
    }

    // ── Step 4b: Verify (frontend signature check) ==>

    /**
     * Called by the frontend after the Razorpay checkout callback.
     * Verifies the signature and returns the current payment status.
     *
     * NOTE: Do NOT call confirmOrder here — that is handled by the webhook
     * (step 4a). This endpoint is for frontend UX only — to show the
     * success/failure screen immediately after checkout, without waiting
     * for the webhook to arrive.
     */
    @Transactional(readOnly = true)
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {
        boolean signatureValid = paymentGateway.verifyPaymentSignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature());

        if (!signatureValid) {
            log.warn("Invalid payment signature from frontend: orderId={}",
                    request.razorpayOrderId());
            throw new DomainException(
                    "Payment verification failed. Signature is invalid.",
                    HttpStatus.BAD_REQUEST);
        }

        // Find the payment record to return current status
        Payment payment = paymentRepository
                .findByGatewayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> ResourceNotFoundException
                        .of("Payment", request.razorpayOrderId()));

        OrderSummary order = orderingModuleApi.getOrderById(payment.getOrderId())
                .orElseThrow(() -> ResourceNotFoundException.of("Order", payment.getOrderId()));

        return new VerifyPaymentResponse(
                payment.getStatus().isSuccessful(),
                payment.getOrderId(),
                order.orderNumber(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCurrencyCode(),
                payment.getCapturedAt(),
                payment.getStatus().isSuccessful()
                        ? "Payment successful! Your order has been confirmed."
                        : "Payment is being processed. You will receive a confirmation shortly."
        );
    }

    // ── Step 4a: Webhook handler ==>

    /**
     * Handles incoming Razorpay webhook events.
     *
     * CRITICAL: signature verification happens in PaymentController BEFORE
     * this method is called. By the time we're here the webhook is authentic.
     *
     * Idempotent — processing the same event twice has no effect:
     *  - payment.captured on an already-CAPTURED payment → no-op
     *  - payment.failed on an already-FAILED/CANCELLED order → no-op
     */
    @Transactional
    public void handleWebhook(String rawPayload) {
        WebhookEvent event = parseWebhookPayload(rawPayload);
        if (event == null || event.payload() == null) {
            log.warn("Received unparseable webhook payload");
            return;
        }

        String eventType = event.event();
        WebhookEvent.WebhookPaymentData data = event.payload().payment().entity();

        log.info("Webhook received: event={} gatewayOrderId={} gatewayPaymentId={}",
                eventType, data.order_id(), data.id());

        switch (eventType) {
            case EVENT_CAPTURED -> handlePaymentCaptured(data, rawPayload);
            case EVENT_FAILED -> handlePaymentFailed(data, rawPayload);
            default -> log.debug("Unhandled webhook event type: {}", eventType);
        }
    }

    // ── Admin ==>

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForOrder(UUID orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    // ── Internal ==>

    private void handlePaymentCaptured(WebhookEvent.WebhookPaymentData data, String rawPayload) {
        // Idempotency: if payment already captured, skip
        if (paymentRepository.findByGatewayPaymentId(data.id())
                .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
                .isPresent()) {
            log.info("Duplicate payment.captured webhook ignored: paymentId={}", data.id());
            return;
        }

        Payment payment = paymentRepository.findByGatewayOrderId(data.order_id())
                .orElseGet(() -> createPaymentFromWebhook(data));

        payment.markCaptured(data.id(), rawPayload);
        paymentRepository.save(payment);

        // Confirm the order — triggers OrderConfirmedEvent → notification email
        orderingModuleApi.confirmOrder(payment.getOrderId());

        // Store gateway payment ID on the order for reference
        log.info("Payment captured: orderId={} paymentId={} amount={}",
                payment.getOrderId(), data.id(), payment.getAmount());
    }

    private void handlePaymentFailed(WebhookEvent.WebhookPaymentData data, String rawPayload) {
        Payment payment = paymentRepository.findByGatewayOrderId(data.order_id())
                .orElseGet(() -> createPaymentFromWebhook(data));

        if (payment.getStatus().isTerminal()) {
            log.info("Duplicate payment.failed webhook ignored: gatewayOrderId={}", data.order_id());
            return;
        }

        String reason = data.error_description() != null
                ? data.error_description()
                : "Payment declined by gateway";

        payment.markFailed(reason, rawPayload);
        paymentRepository.save(payment);

        // Cancel order and restore stock
        orderingModuleApi.cancelOrder(payment.getOrderId(), "Payment failed: " + reason);

        log.info("Payment failed: orderId={} reason={}", payment.getOrderId(), reason);
    }

    private CreatePaymentOrderResponse createNewGatewayOrder(OrderSummary order) {
        GatewayOrderResult gateway = paymentGateway.createOrder(
                order.totalAmount(), order.orderId().toString());

        Payment payment = Payment.builder()
                .orderId(order.orderId())
                .gatewayOrderId(gateway.gatewayOrderId())
                .amount(order.totalAmount())
                .currencyCode(order.currencyCode())
                .build();
        payment = paymentRepository.save(payment);

        // Link gateway order ID to our order record
        orderingModuleApi.setGatewayOrderId(order.orderId(), gateway.gatewayOrderId());

        return buildCreateResponse(payment, order, gateway);
    }

    private CreatePaymentOrderResponse buildCreateResponse(Payment payment, OrderSummary order) {
        return new CreatePaymentOrderResponse(
                payment.getId(),
                payment.getOrderId(),
                order.orderNumber(),
                payment.getGatewayOrderId(),
                null,   // keyId not available without calling gateway again — return null
                payment.getAmount(),
                payment.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue(),
                payment.getCurrencyCode(),
                "Sipora order " + order.orderNumber()
        );
    }

    private CreatePaymentOrderResponse buildCreateResponse(Payment payment, OrderSummary order,
                                                           GatewayOrderResult gateway) {
        return new CreatePaymentOrderResponse(
                payment.getId(),
                payment.getOrderId(),
                order.orderNumber(),
                gateway.gatewayOrderId(),
                gateway.gatewayKeyId(),
                gateway.amount(),
                gateway.amountInPaise(),
                gateway.currency(),
                "Sipora order " + order.orderNumber()
        );
    }

    /**
     * Creates a Payment record from a webhook when no prior record exists.
     * This handles the edge case where the webhook arrives before the
     * frontend calls create-order (race condition on very fast payments).
     */
    private Payment createPaymentFromWebhook(WebhookEvent.WebhookPaymentData data) {
        OrderSummary order = orderingModuleApi
                .getOrderByGatewayOrderId(data.order_id())
                .orElseThrow(() -> new DomainException(
                        "No order found for gateway order: " + data.order_id(),
                        HttpStatus.NOT_FOUND));

        Payment payment = Payment.builder()
                .orderId(order.orderId())
                .gatewayOrderId(data.order_id())
                .amount(order.totalAmount())
                .currencyCode(order.currencyCode())
                .build();
        return paymentRepository.save(payment);
    }

    private WebhookEvent parseWebhookPayload(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, WebhookEvent.class);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            return null;
        }
    }
}