package in.sipora.backend.modules.catalog.domain;

import in.sipora.backend.shared.domain.BaseEntity;
import in.sipora.backend.shared.domain.Money;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a purchasable variant of a Product.
 *
 * For Sipora, examples:
 *   Product: "Sipora Glass Bottle"
 *     Variant 1: SKU=SIP-GB-700-BLU, color=Ocean Blue,  size=700ml, price=INR 899
 *     Variant 2: SKU=SIP-GB-700-GRN, color=Forest Green, size=700ml, price=INR 899
 *
 *   Product: "Citrus Flavor Pod Pack"
 *     Variant 1: SKU=SIP-POD-CIT-6,  flavor=Citrus, qty=6 pods,  price=INR 349
 *     Variant 2: SKU=SIP-POD-CIT-12, flavor=Citrus, qty=12 pods, price=INR 599
 *
 * Attributes (color, size, flavor) are stored as nullable strings.
 * This is intentionally simple — no EAV model needed at this scale.
 *
 * Price is the embedded Money value object from shared kernel.
 * Stock deduction uses optimistic locking (@Version) to prevent overselling.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = @UniqueConstraint(name = "uq_variant_sku", columnNames = "sku")
)
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    /**
     * Human-readable label shown in the variant selector UI.
     * e.g. "Ocean Blue — 700ml" or "Citrus — 6 Pods"
     */
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    // ── Price (embedded Money)

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",       column = @Column(name = "price_amount",   nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money price;

    // ── Stock

    @Column(name = "stock_qty", nullable = false)
    @Builder.Default
    private int stockQty = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private int lowStockThreshold = 5;

    // ── Attributes (Sipora-specific)

    @Column(name = "attr_color", length = 50)
    private String color;

    @Column(name = "attr_size", length = 50)
    private String size;

    @Column(name = "attr_flavor", length = 50)
    private String flavor;

    @Column(name = "attr_quantity_count")
    private Integer quantityCount;    // e.g. 6 pods, 12 pods

    // ── Lifecycle

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    // ── Stock helpers

    public boolean isInStock() {
        return stockQty > 0;
    }

    public boolean isLowStock() {
        return stockQty > 0 && stockQty <= lowStockThreshold;
    }

    /**
     * Deducts qty from stock. Throws if insufficient.
     * Called inside a @Transactional service — relies on DB-level locking
     * via SELECT FOR UPDATE in the repository query.
     */
    public void deductStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Deduct qty must be > 0");
        if (this.stockQty < qty) {
            throw new in.sipora.backend.shared.exception.DomainException(
                    "Insufficient stock for SKU: " + sku + ". Available: " + stockQty,
                    org.springframework.http.HttpStatus.CONFLICT
            );
        }
        this.stockQty -= qty;
    }

    public void restoreStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Restore qty must be > 0");
        this.stockQty += qty;
    }
}