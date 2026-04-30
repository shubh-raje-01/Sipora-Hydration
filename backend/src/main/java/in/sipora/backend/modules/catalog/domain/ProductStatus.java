package in.sipora.backend.modules.catalog.domain;

/**
 * Lifecycle state of a Product.
 *
 * DRAFT      — being set up by admin, not visible on storefront
 * ACTIVE     — live and purchasable
 * OUT_OF_STOCK — visible but cannot be added to cart (all variants at 0 stock)
 * ARCHIVED   — discontinued, hidden from storefront but history preserved
 */
public enum ProductStatus {
    DRAFT,
    ACTIVE,
    OUT_OF_STOCK,
    ARCHIVED
}