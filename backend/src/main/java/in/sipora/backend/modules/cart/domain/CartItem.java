package in.sipora.backend.modules.cart.domain;

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
 * A single line item in a Cart.
 *
 * Price and name snapshots are captured from CatalogModuleApi at the moment
 * the item is added. This ensures:
 *  1. The cart total is stable even if the admin changes a product price.
 *  2. The order module can read unitPrice directly from here without
 *     hitting the catalog again (prices are locked at order creation time).
 *
 * productId and variantId are plain UUIDs — no @ManyToOne to catalog entities.
 * The catalog module is never imported inside cart.domain.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /** Plain UUID reference to catalog.Product — no JPA join across modules. */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /** Plain UUID reference to catalog.ProductVariant. */
    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    // ── Snapshots (captured at add-to-cart time) ==>

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "variant_name", nullable = false, length = 200)
    private String variantName;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "INR";

    @Column(name = "image_url")
    private String imageUrl;

    // ── Mutable ==>

    @Column(name = "quantity", nullable = false)
    private int quantity;

    // ── Computed ==>

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void incrementQuantity(int delta) {
        this.quantity += delta;
    }

    public void setQuantitySafe(int qty) {
        if (qty < 1) throw new IllegalArgumentException("Cart item quantity must be >= 1");
        this.quantity = qty;
    }
}