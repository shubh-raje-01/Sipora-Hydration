package in.sipora.backend.modules.ordering.domain;

import java.util.Set;

/**
 * Finite state machine for order lifecycle.
 *
 * Allowed transitions:
 *
 *   PENDING_PAYMENT → CONFIRMED (payment webhook success)
 *   PENDING_PAYMENT → CANCELLED (payment failed / expired / user cancelled)
 *   CONFIRMED → PROCESSING (admin picks order)
 *   CONFIRMED → CANCELLED (admin cancels before dispatch)
 *   PROCESSING → SHIPPED (admin marks dispatched)
 *   SHIPPED → DELIVERED (delivery confirmed)
 *   CONFIRMED → REFUNDED (admin initiates refund)
 *   DELIVERED → REFUNDED (refund after delivery)
 *
 * Terminal states: DELIVERED, CANCELLED, REFUNDED — no further transitions.
 */
public enum OrderStatus {

    PENDING_PAYMENT,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED;

    private static final java.util.Map<OrderStatus, Set<OrderStatus>> TRANSITIONS =
            new java.util.EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(PENDING_PAYMENT, Set.of(CONFIRMED, CANCELLED));
        TRANSITIONS.put(CONFIRMED, Set.of(PROCESSING, CANCELLED, REFUNDED));
        TRANSITIONS.put(PROCESSING, Set.of(SHIPPED));
        TRANSITIONS.put(SHIPPED, Set.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, Set.of(REFUNDED));
        TRANSITIONS.put(CANCELLED, Set.of());
        TRANSITIONS.put(REFUNDED, Set.of());
    }

    public boolean canTransitionTo(OrderStatus next) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }

    public boolean isCancellable() {
        return this == PENDING_PAYMENT || this == CONFIRMED;
    }
}