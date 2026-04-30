package in.sipora.backend.modules.catalog.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the catalog module.
 *
 * The ONLY interface other modules may import from in.sipora.modules.catalog.*.
 * All domain entities (Product, ProductVariant, Category) are strictly internal.
 * Enforced by ArchUnit ModuleBoundaryTest.
 *
 * Stock mutation methods (deductStock, restoreStock) are intentionally
 * synchronous and @Transactional on the impl side — ordering module
 * calls them inside its own transaction when placing or cancelling an order.
 */
public interface CatalogModuleApi {

    /** Fetch a product with all variants. Returns empty if not found or inactive. */
    Optional<ProductSummary> getProductById(UUID productId);

    /** Fetch a product by URL slug. Used by AI and cart to resolve slugs. */
    Optional<ProductSummary> getProductBySlug(String slug);

    /** Fetch a specific variant. Returns empty if variant doesn't exist. */
    Optional<VariantSummary> getVariantById(UUID variantId);

    /**
     * Returns true if the given variant exists, belongs to the given product,
     * and has at least the requested quantity available.
     */
    boolean isVariantAvailable(UUID variantId, int requestedQty);

    /**
     * Atomically deducts stock for a variant.
     * Called by OrderService when an order is placed.
     * Throws DomainException (HTTP 409) if insufficient stock.
     */
    void deductStock(UUID variantId, int qty);

    /**
     * Restores stock for a variant.
     * Called by OrderService when an order is cancelled or payment fails.
     */
    void restoreStock(UUID variantId, int qty);

    /** Fetch multiple products by their IDs. Used by the AI advisor. */
    List<ProductSummary> getProductsByIds(List<UUID> productIds);

    /** Returns a flat list of active products for AI context injection. */
    List<ProductSummary> getAllActiveProducts();
}