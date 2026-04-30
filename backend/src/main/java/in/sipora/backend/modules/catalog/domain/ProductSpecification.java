package in.sipora.backend.modules.catalog.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Specification factory for dynamic product filtering.
 *
 * Usage in ProductService:
 *   Specification<Product> spec = ProductSpecification.active()
 *       .and(ProductSpecification.inCategory(categoryId))
 *       .and(ProductSpecification.priceBetween(min, max))
 *       .and(ProductSpecification.inStock());
 *
 *   Page<Product> page = productRepository.findAll(spec, pageable);
 *
 * Each method returns a Specification that can be composed with .and() / .or().
 * Null-safe — if a filter parameter is null the specification is not applied.
 */
public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> active() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), ProductStatus.ACTIVE);
    }

    public static Specification<Product> featured() {
        return (root, query, cb) ->
                cb.isTrue(root.get("featured"));
    }

    public static Specification<Product> inCategory(UUID categoryId) {
        // Return a lambda that yields a null predicate — JPA treats null as "no restriction".
        if (categoryId == null) return (root, query, cb) -> null;
        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> inStock() {
        return (root, query, cb) -> {
            Join<Product, ProductVariant> variants = root.join("variants", JoinType.INNER);
            // pagination. Calling query.distinct(true) without this guard causes NPE at runtime.
            if (query != null) query.distinct(true);
            return cb.and(
                    cb.isTrue(variants.get("active")),
                    cb.greaterThan(variants.get("stockQty"), 0)
            );
        };
    }

    /**
     * Filters by minimum variant price across any active variant.
     * Uses a sub-join so the product appears if ANY variant meets the threshold.
     */
    public static Specification<Product> minPrice(BigDecimal min) {
        // FIX (line 60): same Specification.where(null) removal
        if (min == null) return (root, query, cb) -> null;
        return (root, query, cb) -> {
            Join<Product, ProductVariant> variants = root.join("variants", JoinType.INNER);
            // BONUS FIX: guard against null query in count queries
            if (query != null) query.distinct(true);
            return cb.and(
                    cb.isTrue(variants.get("active")),
                    cb.greaterThanOrEqualTo(variants.get("price").get("amount"), min)
            );
        };
    }

    public static Specification<Product> maxPrice(BigDecimal max) {
        if (max == null) return (root, query, cb) -> null;
        return (root, query, cb) -> {
            Join<Product, ProductVariant> variants = root.join("variants", JoinType.INNER);
            // BONUS FIX: guard against null query in count queries
            if (query != null) query.distinct(true);
            return cb.and(
                    cb.isTrue(variants.get("active")),
                    cb.lessThanOrEqualTo(variants.get("price").get("amount"), max)
            );
        };
    }

    public static Specification<Product> nameContains(String keyword) {

        if (keyword == null || keyword.isBlank()) return (root, query, cb) -> null;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("shortDescription")), pattern)
                );
    }
}