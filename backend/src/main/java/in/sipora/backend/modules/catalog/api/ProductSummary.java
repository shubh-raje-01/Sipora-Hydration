package in.sipora.backend.modules.catalog.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight product projection shared across module boundaries.
 *
 * Plain Java record — no JPA, no Jackson annotations.
 * Other modules (cart, ordering, ai, review) receive this from
 * CatalogModuleApi and must never import catalog.domain.Product directly.
 *
 * startingPrice = the lowest variant price, in INR.
 * inStock       = true if at least one variant has stockQty > 0.
 */
public record ProductSummary(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal startingPrice,
        String currencyCode,
        boolean inStock,
        int totalStock,
        String primaryImageUrl,
        List<String> categoryNames
) {}