package in.sipora.backend.modules.catalog.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core product entity for Sipora's catalog.
 *
 * A Product is the parent record (e.g. "Sipora Glass Bottle 700ml").
 * Price and stock live on ProductVariant (e.g. "Ocean Blue", "Forest Green").
 *
 * Images are stored as Cloudinary URLs in an ordered ElementCollection.
 * images[0] is always treated as the primary display image.
 *
 * metaTitle / metaDescription are used for SEO in the React frontend.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uq_products_slug", columnNames = "slug")
)
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 280)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    // ── Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "product_images",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", nullable = false)
    @OrderColumn(name = "image_order")
    private List<String> images = new ArrayList<>();

    // ── Computed helpers

    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    public String getPrimaryImageUrl() {
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * Lowest price across all active variants.
     * Returns null if there are no variants.
     */
    public BigDecimal getStartingPrice() {
        return variants.stream()
                .filter(v -> v.isActive())
                .map(v -> v.getPrice().getAmount())
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    public int getTotalStock() {
        return variants.stream()
                .filter(ProductVariant::isActive)
                .mapToInt(ProductVariant::getStockQty)
                .sum();
    }

    public boolean isInStock() {
        return variants.stream()
                .filter(ProductVariant::isActive)
                .anyMatch(v -> v.getStockQty() > 0);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
}