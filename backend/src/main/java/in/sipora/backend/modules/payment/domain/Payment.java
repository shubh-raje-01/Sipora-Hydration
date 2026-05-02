package in.sipora.backend.modules.payment.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Records every payment attempt against an order.
 *
 * One order can have multiple Payment rows if the customer attempts payment
 * multiple times (first attempt fails → second attempt succeeds).
 * Only one CAPTURED payment per order is valid — enforced by business logic
 * in PaymentService, not by a DB constraint (to allow multiple failed attempts).
 *
 * Cross-module notes:
 *  - orderId is a plain UUID — no @ManyToOne to ordering.Order
 *  - gatewayOrderId and gatewayPaymentId are Razorpay identifiers
 *
 * webhookPayload stores the raw JSON body of the Razorpay webhook for
 * audit and debugging. Useful if you need to replay a missed webhook.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payment_gateway_payment_id",
                columnNames = "gateway_payment_id"
        )
)
public class Payment extends BaseEntity {

    /** Plain UUID — no FK to ordering.orders (cross-module boundary). */
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    /** Razorpay order ID — e.g. "order_OBJiMHHaxIbgFr". */
    @Column(name = "gateway_order_id", nullable = false, length = 100)
    private String gatewayOrderId;

    /**
     * Razorpay payment ID — e.g. "pay_OBJiMHHaxIbgFr".
     * Null until payment is captured or failed.
     */
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Always "INR" for Sipora India. Stored for future multi-currency support. */
    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Razorpay error code or description on failure. */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /** Raw Razorpay webhook JSON body — stored for audit / replay. */
    @Column(name = "webhook_payload", columnDefinition = "TEXT")
    private String webhookPayload;

    @Column(name = "captured_at")
    private Instant capturedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    // ── Domain helpers ==>

    public void markCaptured(String gatewayPaymentId, String rawWebhookPayload) {
        this.gatewayPaymentId = gatewayPaymentId;
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = Instant.now();
        this.webhookPayload = rawWebhookPayload;
    }

    public void markFailed(String reason, String rawWebhookPayload) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = Instant.now();
        this.webhookPayload = rawWebhookPayload;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }
}