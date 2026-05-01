package in.sipora.backend.modules.cart.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shopping cart entity. Supports two modes:
 *
 *  Guest cart  — userId is null, sessionId is set (UUID from frontend localStorage)
 *  User cart   — userId is set, sessionId may or may not be set
 *
 * On login, CartService.mergeCarts() copies guest cart items into the
 * user's active cart, then marks the guest cart ABANDONED.
 *
 * Cross-module boundary:
 *  userId is stored as a plain UUID column — NOT a @ManyToOne to identity.User.
 *  The identity module is never imported inside catalog.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    /** Null for guest carts. Plain UUID — no FK to users table. */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Frontend-generated UUID stored in localStorage.
     * Allows anonymous browsing with a persistent cart across page refreshes.
     * Cleared once the cart is merged into a user cart.
     */
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // ── Domain helpers ==>

    public boolean isActive() { return status == CartStatus.ACTIVE; }
    public boolean isGuest() { return userId == null; }
    public boolean isEmpty() { return items == null || items.isEmpty(); }

    public int getTotalItemCount() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /** Finds an existing item by variantId — used to increment qty instead of duplicate. */
    public java.util.Optional<CartItem> findItemByVariantId(UUID variantId) {
        return items.stream()
                .filter(i -> i.getVariantId().equals(variantId))
                .findFirst();
    }
}