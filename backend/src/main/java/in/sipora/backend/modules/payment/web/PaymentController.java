package in.sipora.backend.modules.payment.web;

import in.sipora.backend.modules.payment.application.PaymentService;
import in.sipora.backend.modules.payment.infrastructure.PaymentGateway;
import in.sipora.backend.modules.payment.web.PaymentDTOs.CreatePaymentOrderRequest;
import in.sipora.backend.modules.payment.web.PaymentDTOs.CreatePaymentOrderResponse;
import in.sipora.backend.modules.payment.web.PaymentDTOs.PaymentResponse;
import in.sipora.backend.modules.payment.web.PaymentDTOs.VerifyPaymentRequest;
import in.sipora.backend.modules.payment.web.PaymentDTOs.VerifyPaymentResponse;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.util.SecurityUtils;
import in.sipora.backend.shared.web.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Payment REST endpoints.
 *
 * Security model:
 *
 *  /create-order  — JWT auth required (user must own the order)
 *  /verify        — JWT auth required
 *  /webhook       — NO JWT (Razorpay can't send a JWT)
 *                   Instead: HMAC-SHA256 signature verified in this controller
 *                   BEFORE any payload parsing or business logic.
 *                   Whitelisted in SecurityConfig.PUBLIC_POST.
 *
 * IMPORTANT: The webhook endpoint reads the raw request body as bytes
 * before Spring's Jackson parser touches it. The HMAC is computed on the
 * raw bytes. Parsing first and re-serialising changes whitespace and would
 * break the signature check.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay payment initiation, verification and webhooks")
public class PaymentController {

    private static final String RAZORPAY_SIGNATURE_HEADER = "X-Razorpay-Signature";

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    // ── Step 2: Initiate payment ==>

    @PostMapping("/create-order")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a Razorpay order for a placed order",
            description = """
            Call this after POST /api/v1/orders to get the Razorpay order details.
            Returns razorpayOrderId, keyId, amount in paise — pass all three to the
            Razorpay checkout SDK:

              const rzp = new Razorpay({
                key:       response.razorpayKeyId,
                amount:    response.amountInPaise,
                currency:  response.currency,
                order_id:  response.razorpayOrderId,
                handler:   r => verifyPayment(r)
              });
              rzp.open();
            """
    )
    public ResponseEntity<ApiResponse<CreatePaymentOrderResponse>> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        CreatePaymentOrderResponse response =
                paymentService.createPaymentOrder(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created", response));
    }

    // ── Step 4b: Frontend signature verify ==>

    @PostMapping("/verify")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Verify payment signature after Razorpay checkout callback",
            description = """
            Call this from the Razorpay checkout handler function with:
              razorpay_order_id, razorpay_payment_id, razorpay_signature

            Returns whether payment was captured and the current order status.
            Use this to show the success/failure screen immediately.
            The authoritative order confirmation comes from the webhook.
            """
    )
    public ResponseEntity<ApiResponse<VerifyPaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        VerifyPaymentResponse response = paymentService.verifyPayment(request);
        String message = response.success()
                ? "Payment verified successfully"
                : "Payment verification complete — awaiting gateway confirmation";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // ── Step 4a: Razorpay webhook ==>

    /**
     * Receives payment events from Razorpay servers.
     *
     * Security: This endpoint is public (no JWT) because Razorpay cannot
     * authenticate as our users. Instead, authenticity is guaranteed by
     * verifying the HMAC-SHA256 signature of the raw body using our
     * webhook secret. If the signature is invalid, we return 400 immediately
     * and log a warning — someone is sending spoofed requests.
     *
     * Always returns 200 to Razorpay (even on errors) so they don't
     * keep retrying. Idempotency is handled inside PaymentService.
     */
    @PostMapping("/webhook")
    @Operation(
            summary = "Razorpay webhook — DO NOT call manually",
            description = "Server-to-server endpoint for Razorpay payment events. " +
                    "Configure this URL in your Razorpay dashboard under Webhooks."
    )
    public ResponseEntity<Void> handleWebhook(
            HttpServletRequest httpRequest,
            @RequestHeader(value = RAZORPAY_SIGNATURE_HEADER, required = false)
            String razorpaySignature) {

        // 1 — Read raw body BEFORE any parsing (required for HMAC verification)
        String rawPayload = readRawBody(httpRequest);

        // 2 — Signature must be present
        if (razorpaySignature == null || razorpaySignature.isBlank()) {
            log.warn("Webhook received without {} header — rejecting", RAZORPAY_SIGNATURE_HEADER);
            return ResponseEntity.badRequest().build();
        }

        // 3 — Verify HMAC BEFORE doing any business logic
        if (!paymentGateway.verifyWebhookSignature(rawPayload, razorpaySignature)) {
            log.warn("Webhook HMAC verification failed — possible spoofed request. " +
                    "Signature: {}", razorpaySignature);
            return ResponseEntity.badRequest().build();
        }

        // 4 — Signature is valid — process the event
        try {
            paymentService.handleWebhook(rawPayload);
        } catch (Exception e) {
            // Log but still return 200 — prevents Razorpay from infinite retries
            // on genuinely unexpected errors. Check logs for investigation.
            log.error("Error processing webhook payload: {}", e.getMessage(), e);
        }

        // Always 200 to acknowledge receipt
        return ResponseEntity.ok().build();
    }

    // ── Admin ==>

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Get all payment attempts for an order")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsForOrder(
            @PathVariable UUID orderId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Payments retrieved",
                paymentService.getPaymentsForOrder(orderId)));
    }

    // ── Helper ==>

    /**
     * Reads the raw request body as a String without triggering Spring's
     * body consumption (which would prevent later JSON parsing).
     *
     * We read bytes directly from the InputStream — safe because the
     * webhook endpoint doesn't need a parsed @RequestBody.
     */
    private String readRawBody(HttpServletRequest request) {
        try {
            return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read webhook request body", e);
            throw new DomainException("Failed to read request body", HttpStatus.BAD_REQUEST);
        }
    }
}