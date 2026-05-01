package in.sipora.backend.modules.cart.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Cart snapshot consumed by the ordering module when placing an order.
 *
 * The ordering module reads this once, locks in the line items and totals,
 * then calls CartModuleApi.clearCart() to empty the cart.
 *
 * userId may be null only for guest carts — but ordering always requires
 * an authenticated user so in practice it is always non-null at checkout.
 */
public record CartSummary(
        UUID cartId,
        UUID userId,
        List<CartItemSummary> items,
        int  totalItems,
        BigDecimal subtotal,
        String currencyCode
) {
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}