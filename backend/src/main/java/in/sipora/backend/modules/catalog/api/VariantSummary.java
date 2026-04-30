package in.sipora.backend.modules.catalog.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight variant projection for cross-module use.
 * Cart and ordering modules use this to snapshot price at time of purchase.
 */
public record VariantSummary(
        UUID id,
        UUID productId,
        String sku,
        String displayName,   // e.g. "700ml — Ocean Blue"
        BigDecimal price,
        String currencyCode,
        int stockQty,
        boolean inStock
) {}