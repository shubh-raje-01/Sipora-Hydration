package in.sipora.backend.modules.ordering.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core order entity.
 *
 * Cross-module notes:
 *  - userId is a plain UUID — no @ManyToOne to identity.User
 *  - cartId is stored for reference only — cart module is not imported
 *  - gatewayOrderId is set by PaymentService after calling Razorpay
 *  - Items carry price snapshots from CartItemSummary at placement time
 *
 * Status transitions are guarded by OrderStatus.canTransitionTo().
 * The transition() method is the single mutation point for status changes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_order_number",           columnNames = "order_number"),
                @UniqueConstraint(name = "uq_order_gateway_order_id", columnNames = "gateway_order_id")
        }
)
public class Order extends BaseEntity {

    /** Human-readable order number shown to customers. e.g. SIP-20250423-0001 */
    @Column(name = "order_number", nullable = false, length = 30, updatable = false)
    private String orderNumber;

    /** Plain UUID — no FK to identity.users (cross-module boundary). */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** Stored for audit trail — not used to reload the cart (cart is cleared). */
    @Column(name = "cart_id", updatable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    // ── Financials ==>

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_charge", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "INR";

    // ── Payment gateway ==>

    /** Set by PaymentService after creating the Razorpay order. */
    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    /** Set by PaymentService on successful payment webhook. */
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    // ── Shipping ==>

    @Embedded
    private ShippingAddress shippingAddress;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    // ── Cancellation ==>

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // ── Items ==>

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // ── Domain behaviour ==>

    /**
     * Enforces the state machine. All status changes must go through here.
     *
     * //@throws in.backend.sipora.shared.exception.DomainException (409) if the transition is invalid
     */
    public void transition(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new in.sipora.backend.shared.exception.DomainException(
                    "Cannot transition order from %s to %s".formatted(this.status, next),
                    HttpStatus.CONFLICT
            );
        }
        this.status = next;

        // Side-effect timestamps
        switch (next) {
            case SHIPPED -> this.shippedAt   = Instant.now();
            case DELIVERED -> this.deliveredAt = Instant.now();
            case CANCELLED -> this.cancelledAt = Instant.now();
            default -> { /* no timestamp side-effect */ }
        }
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public boolean isCancellable() {
        return status.isCancellable();
    }

    public boolean belongsTo(UUID uid) {
        return this.userId.equals(uid);
    }
}