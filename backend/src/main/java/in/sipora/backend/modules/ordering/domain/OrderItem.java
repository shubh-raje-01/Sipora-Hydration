package in.sipora.backend.modules.ordering.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single line item within an Order.
 *
 * All monetary values and names are copied from CartItemSummary at order
 * placement time. They are immutable — price changes, product renames or
 * variant deletions in the catalog have no effect on historical orders.
 *
 * productId and variantId are plain UUIDs — no JPA join to catalog entities.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ── Catalog references (plain UUIDs) ==>

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "variant_id", nullable = false, updatable = false)
    private UUID variantId;

    // ── Snapshots ==>

    @Column(name = "product_name", nullable = false, updatable = false, length = 255)
    private String productName;

    @Column(name = "variant_name", nullable = false, updatable = false, length = 200)
    private String variantName;

    @Column(name = "sku", nullable = false, updatable = false, length = 100)
    private String sku;

    @Column(name = "unit_price", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "currency_code", nullable = false, updatable = false, length = 3)
    @Builder.Default
    private String currencyCode = "INR";

    @Column(name = "image_url", updatable = false)
    private String imageUrl;

    // ── Quantity (immutable after placement) ==>

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    // ── Computed ==>

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}