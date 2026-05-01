package in.sipora.backend.modules.cart.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Individual cart line item — shared with ordering module.
 * Prices here are snapshots taken when the item was added to cart.
 */
public record CartItemSummary(
        UUID cartItemId,
        UUID productId,
        UUID variantId,
        String productName,
        String variantName,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String currencyCode
) {}