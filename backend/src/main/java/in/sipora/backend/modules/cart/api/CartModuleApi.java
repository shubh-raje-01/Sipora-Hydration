package in.sipora.backend.modules.cart.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the cart module.
 *
 * Only OrderService calls this — to snapshot cart contents at checkout
 * and to clear the cart once an order has been successfully created.
 *
 * No other module depend on this interface. Cart web endpoints
 * are consumed directly by the React frontend, not by other modules.
 */
public interface CartModuleApi {

    /**
     * Returns the active cart for a user, with all items and price snapshots.
     * Returns empty if the user has no active cart or the cart is empty.
     */
    Optional<CartSummary> getCartForUser(UUID userId);

    /**
     * Marks the cart as CHECKED_OUT and removes all items.
     * Called by OrderService immediately after a successful order creation.
     *
     * @param userId the authenticated user's ID
     * @param cartId the specific cart that was checked out (prevents race conditions)
     */
    void clearCart(UUID userId, UUID cartId);
}