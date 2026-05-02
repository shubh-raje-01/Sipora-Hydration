package in.sipora.backend.modules.payment.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * All request and response records for the payment module web layer.
 */
public final class PaymentDTOs {

    private PaymentDTOs() {}

    // Requests ==>

    /**
     * Step 1 — Frontend calls this after order placement to get the
     * Razorpay order details needed to open the checkout modal.
     */
    public record CreatePaymentOrderRequest(
            @NotNull(message = "orderId is required")
            UUID orderId
    ) {}

    /**
     * Step 2 — Frontend calls this after the Razorpay checkout callback
     * to verify the payment signature on the server side.
     *
     * Fields match the Razorpay checkout handler response object exactly.
     */
    public record VerifyPaymentRequest(
            @NotBlank
            @JsonProperty("razorpay_order_id")
            String razorpayOrderId,

            @NotBlank
            @JsonProperty("razorpay_payment_id")
            String razorpayPaymentId,

            @NotBlank
            @JsonProperty("razorpay_signature")
            String razorpaySignature
    ) {}

    /**
     * Internal record for parsing the Razorpay webhook JSON body.
     * Razorpay sends different event types — we care about:
     *   payment.captured → confirm order
     *   payment.failed → cancel order
     */
    public record WebhookEvent(
            String event,
            WebhookPayload payload
    ) {
        public record WebhookPayload(WebhookPaymentEntity payment) {}
        public record WebhookPaymentEntity(WebhookPaymentData entity) {}
        public record WebhookPaymentData(
                String id,           // payment_id
                String order_id,     // gateway order_id
                String status,
                Long amount,       // in paise
                String currency,
                String error_code,
                String error_description
        ) {}
    }

    // Responses ==>

    /**
     * Returned to frontend after step 1 (createPaymentOrder).
     * All fields are consumed by the Razorpay checkout SDK.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CreatePaymentOrderResponse(
            UUID paymentRecordId,    // our internal Payment.id (for tracking)
            UUID orderId,
            String orderNumber,
            String razorpayOrderId,    // pass to Razorpay SDK as order_id
            String razorpayKeyId,      // pass to Razorpay SDK as key
            BigDecimal amount,             // display amount in INR
            long amountInPaise,      // pass to Razorpay SDK as amount
            String currency,
            String description         // shown in Razorpay checkout UI
    ) {}

    /**
     * Returned to frontend after step 2 (verifyPayment).
     * Frontend uses this to show the success or failure screen.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerifyPaymentResponse(
            boolean success,
            UUID orderId,
            String orderNumber,
            String status,             // CAPTURED or FAILED
            BigDecimal amount,
            String currencyCode,
            Instant capturedAt,
            String message
    ) {}

    /** Returned for payment history / status queries. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentResponse(
            UUID paymentId,
            UUID orderId,
            String status,
            BigDecimal amount,
            String currencyCode,
            String gatewayOrderId,
            String gatewayPaymentId,
            String failureReason,
            Instant capturedAt,
            Instant failedAt,
            Instant createdAt
    ) {}
}