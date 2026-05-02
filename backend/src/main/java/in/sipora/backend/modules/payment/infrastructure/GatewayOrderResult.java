package in.sipora.backend.modules.payment.infrastructure;

import java.math.BigDecimal;

/**
 * Result of creating an order on the payment gateway.
 *
 * All fields are required by the Razorpay checkout SDK on the frontend:
 *
 * const options = {
 *   key: gatewayKeyId,      // "rzp_test_xxx"
 *   amount: amountInPaise,     // 49900 for INR 499
 *   currency: currency,          // "INR"
 *   order_id: gatewayOrderId,    // "order_OBJiMHHaxIbgFr"
 *   ...
 * };
 * const rzp = new Razorpay(options);
 * rzp.open();
 */
public record GatewayOrderResult(
        String gatewayOrderId,    // Razorpay order_id
        String gatewayKeyId,      // public key for frontend SDK init
        BigDecimal amount,            // original INR amount (for display)
        long amountInPaise,     // amount × 100 (for Razorpay SDK)
        String currency           // "INR"
) {}