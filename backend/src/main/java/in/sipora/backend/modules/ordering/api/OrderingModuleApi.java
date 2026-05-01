package in.sipora.backend.modules.ordering.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the ordering module.
 *
 * Callers:
 *  - payment module → confirmOrder(), cancelOrder()
 *  - review module → hasUserPurchasedProduct()
 *
 * The payment module calls confirmOrder() inside its webhook handler
 * after verifying the Razorpay HMAC signature and payment amount.
 * If payment fails or times out, it calls cancelOrder() which triggers
 * stock restoration via CatalogModuleApi inside the ordering module.
 */
public interface OrderingModuleApi {

    /** Fetch an order by its ID. Returns empty if not found. */
    Optional<OrderSummary> getOrderById(UUID orderId);

    /** Fetch an order by its Razorpay gateway order ID (set at payment initiation). */
    Optional<OrderSummary> getOrderByGatewayOrderId(String gatewayOrderId);

    /**
     * Transitions PENDING_PAYMENT → CONFIRMED.
     * Called by PaymentService on successful payment webhook.
     * Publishes OrderConfirmedEvent so the notification module sends a receipt email.
     *
     * // @throws DomainException (409) if the order is not in the PENDING_PAYMENT state
     */
    void confirmOrder(UUID orderId);

    /**
     * Transitions PENDING_PAYMENT or CONFIRMED → CANCELLED.
     * Called by PaymentService on failed/expired payment.
     * Restores stock for all order items via CatalogModuleApi.
     *
     * //@throws DomainException (409) if the order cannot be cancelled in its current state
     */
    void cancelOrder(UUID orderId, String reason);

    /**
     * Sets the Razorpay gateway order ID on an order.
     * Called by PaymentService after creating the Razorpay order.
     */
    void setGatewayOrderId(UUID orderId, String gatewayOrderId);

    /**
     * Returns true if the user has at least one DELIVERED order containing the given product.
     * Used by ReviewService to enforce "verified purchase" before allowing a review.
     */
    boolean hasUserPurchasedProduct(UUID userId, UUID productId);

    /** Returns all orders for a user, newest first. Used for order history page. */
    List<OrderSummary> getOrdersByUserId(UUID userId);
}