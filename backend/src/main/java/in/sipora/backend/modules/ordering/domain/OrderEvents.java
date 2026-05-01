package in.sipora.backend.modules.ordering.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain events published by the ordering module.
 *
 * The notification module listens to these via @EventListener.
 * Publishing happens via ApplicationEventPublisher — the ordering module
 * has zero knowledge of who is listening, keeping coupling strictly one-way.
 *
 * Both events are plain records — no Spring or JPA dependencies.
 */
public final class OrderEvents {

    private OrderEvents() {}

    /**
     * Published when a new order is placed (status = PENDING_PAYMENT).
     * Notification module sends an "Order Received" acknowledgement email.
     */
    public record OrderPlacedEvent(
            UUID orderId,
            String orderNumber,
            UUID userId,
            BigDecimal totalAmount,
            String currencyCode,
            String userEmail,       // denormalised to avoid cross-module lookup in listener
            String userName
    ) {}

    /**
     * Published when payment is confirmed (status = CONFIRMED).
     * Notification module sends the payment receipt / order confirmation email.
     */
    public record OrderConfirmedEvent(
            UUID orderId,
            String orderNumber,
            UUID userId,
            BigDecimal totalAmount,
            String currencyCode,
            String userEmail,
            String userName
    ) {}

    /**
     * Published when an order is cancelled.
     * Notification module sends a cancellation email.
     */
    public record OrderCancelledEvent(
            UUID orderId,
            String orderNumber,
            UUID userId,
            String reason,
            String userEmail,
            String userName
    ) {}
}