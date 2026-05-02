package in.sipora.backend.modules.payment.domain;

/**
 * Lifecycle states of a Payment record.
 *
 * PENDING — Razorpay order created, awaiting customer payment
 * CAPTURED — Payment received and confirmed via webhook
 * FAILED — Payment failed, declined or timed out
 * REFUNDED — Amount refunded (full or partial) after a successful capture
 *
 * Note: REFUNDED is a terminal state. A new Payment record is created
 * for any subsequent re-payment attempt on the same order.
 */
public enum PaymentStatus {
    PENDING,
    CAPTURED,
    FAILED,
    REFUNDED;

    public boolean isTerminal() {
        return this == CAPTURED || this == FAILED || this == REFUNDED;
    }

    public boolean isSuccessful() {
        return this == CAPTURED;
    }
}