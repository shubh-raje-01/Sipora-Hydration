package in.sipora.backend.modules.payment.infrastructure;

import java.math.BigDecimal;

/**
 * Port interface for the payment gateway adapter.
 *
 * PaymentService depends on this interface, not on RazorpayGateway directly.
 * This means:
 *  - Unit tests can mock the gateway without touching Razorpay's API
 *  - Swapping to Stripe, PayU or CCAvenue = implement this interface + swap the @Bean
 *  - Test mode vs live mode = same code, different credentials in application.yml
 */
public interface PaymentGateway {

    /**
     * Creates an order on the payment gateway side.
     * Must be called before presenting the checkout UI to the customer.
     *
     * @param amount    order total in INR (e.g. 499.00)
     * @param receiptId idempotency key — use our internal order UUID string
     * @return gateway order details needed by the frontend to open the checkout
     */
    GatewayOrderResult createOrder(BigDecimal amount, String receiptId);

    /**
     * Verifies the payment signature sent by the frontend after checkout.
     * This is the client-side verification step.
     *
     * @param gatewayOrderId   Razorpay order ID from createOrder response
     * @param gatewayPaymentId Razorpay payment ID from checkout callback
     * @param signature        Razorpay signature from checkout callback
     * @return true if signature is valid (payment is genuine)
     */
    boolean verifyPaymentSignature(String gatewayOrderId, String gatewayPaymentId,
                                   String signature);

    /**
     * Verifies the HMAC-SHA256 signature of an incoming webhook payload.
     * Must be called before processing any webhook event.
     *
     * @param rawPayload  raw request body (bytes, not parsed)
     * @param signature   value of X-Razorpay-Signature header
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String rawPayload, String signature);
}