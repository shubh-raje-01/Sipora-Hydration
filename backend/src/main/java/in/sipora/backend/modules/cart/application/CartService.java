package in.sipora.backend.modules.cart.application;

import in.sipora.backend.modules.cart.domain.Cart;
import in.sipora.backend.modules.cart.domain.CartItem;
import in.sipora.backend.modules.cart.domain.CartItemRepository;
import in.sipora.backend.modules.cart.domain.CartRepository;
import in.sipora.backend.modules.cart.domain.CartStatus;
import in.sipora.backend.modules.cart.web.CartDTOs.AddItemRequest;
import in.sipora.backend.modules.cart.web.CartDTOs.CartResponse;
import in.sipora.backend.modules.cart.web.CartDTOs.MergeCartRequest;
import in.sipora.backend.modules.cart.web.CartDTOs.UpdateItemRequest;
import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.catalog.api.ProductSummary;
import in.sipora.backend.modules.catalog.api.VariantSummary;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core cart service.
 *
 * Cart resolution strategy:
 *  - Authenticated requests  → resolved by userId
 *  - Guest requests          → resolved by sessionId header (X-Session-Id)
 *  - Both present            → userId takes priority
 *
 * Add-to-cart flow:
 *  1. Resolve or create cart
 *  2. Validate variant exists and has stock (CatalogModuleApi)
 *  3. If variant already in cart → increment quantity
 *  4. Else → create new CartItem with price/name snapshot
 *  5. Validate cart max rules (max 10 distinct items, max 20 qty per item)
 *
 * Merge flow (called from AuthController after successful login):
 *  1. Find guest cart by sessionId
 *  2. Find or create user's active cart
 *  3. For each guest item: if variant already in user cart → add quantities, else move item
 *  4. Mark guest cart ABANDONED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private static final int MAX_DISTINCT_ITEMS = 10;
    private static final int MAX_QTY_PER_ITEM   = 20;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogModuleApi catalogModuleApi;
    private final CartMapper cartMapper;

    // ── Get cart ==>

    @Transactional(readOnly = true)
    public CartResponse getCartForUser(UUID userId) {
        return cartMapper.toResponse(resolveUserCart(userId));
    }

    @Transactional(readOnly = true)
    public CartResponse getGuestCart(String sessionId) {
        Cart cart = cartRepository.findActiveGuestCart(sessionId)
                .orElseGet(() -> Cart.builder().sessionId(sessionId).build());
        return cartMapper.toResponse(cart);
    }

    // ── Add item ==>

    @Transactional
    public CartResponse addItemForUser(UUID userId, AddItemRequest request) {
        Cart cart = resolveOrCreateUserCart(userId);
        addItemToCart(cart, request);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse addItemForGuest(String sessionId, AddItemRequest request) {
        Cart cart = cartRepository.findActiveGuestCart(sessionId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().sessionId(sessionId).build()));
        addItemToCart(cart, request);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    // ── Update item ==>

    @Transactional
    public CartResponse updateItem(UUID userId, UUID cartItemId, UpdateItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(cartItemId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("CartItem", cartItemId));

        if (!catalogModuleApi.isVariantAvailable(item.getVariantId(), request.quantity())) {
            throw new DomainException(
                    "Requested quantity not available for: " + item.getVariantName(),
                    HttpStatus.CONFLICT);
        }

        item.setQuantitySafe(request.quantity());
        cartItemRepository.save(item);
        return cartMapper.toResponse(resolveUserCart(userId));
    }

    // ── Remove item ==>

    @Transactional
    public CartResponse removeItem(UUID userId, UUID cartItemId) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(cartItemId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("CartItem", cartItemId));

        Cart cart = item.getCart();
        cart.removeItem(item);
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    // ── Clear cart ==>

    @Transactional
    public void clearCartForUser(UUID userId) {
        List<Cart> carts = cartRepository.findActiveByUserId(userId);
        carts.forEach(c -> {
            cartItemRepository.deleteAllByCartId(c.getId());
            c.setStatus(CartStatus.CHECKED_OUT);
            cartRepository.save(c);
        });
    }

    // ── Merge guest → user cart on login ==>

    @Transactional
    public CartResponse mergeCarts(UUID userId, MergeCartRequest request) {
        Optional<Cart> guestCartOpt =
                cartRepository.findActiveGuestCart(request.sessionId());

        if (guestCartOpt.isEmpty() || guestCartOpt.get().isEmpty()) {
            // Nothing to merge — just return user's current cart
            return cartMapper.toResponse(resolveOrCreateUserCart(userId));
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart  = resolveOrCreateUserCart(userId);

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existing =
                    userCart.findItemByVariantId(guestItem.getVariantId());

            if (existing.isPresent()) {
                // Add quantities — cap at max
                int merged = Math.min(
                        existing.get().getQuantity() + guestItem.getQuantity(),
                        MAX_QTY_PER_ITEM);
                existing.get().setQuantitySafe(merged);
            } else {
                if (userCart.getItems().size() >= MAX_DISTINCT_ITEMS) {
                    log.info("Cart merge: skipping item {} — user cart at max capacity",
                            guestItem.getVariantId());
                    continue;
                }
                CartItem transferred = CartItem.builder()
                        .productId(guestItem.getProductId())
                        .variantId(guestItem.getVariantId())
                        .productName(guestItem.getProductName())
                        .variantName(guestItem.getVariantName())
                        .sku(guestItem.getSku())
                        .unitPrice(guestItem.getUnitPrice())
                        .currencyCode(guestItem.getCurrencyCode())
                        .imageUrl(guestItem.getImageUrl())
                        .quantity(guestItem.getQuantity())
                        .build();
                userCart.addItem(transferred);
            }
        }

        guestCart.setStatus(CartStatus.ABANDONED);
        cartRepository.save(guestCart);
        Cart saved = cartRepository.save(userCart);

        log.info("Cart merged: sessionId={} → userId={}", request.sessionId(), userId);
        return cartMapper.toResponse(saved);
    }

    // ── Internal helpers ==>

    /**
     * Adds or increments a line item in the cart.
     * Validates stock and cart capacity before mutating.
     */
    private void addItemToCart(Cart cart, AddItemRequest request) {
        // 1. Validate variant exists in catalog
        VariantSummary variant = catalogModuleApi.getVariantById(request.variantId())
                .orElseThrow(() -> ResourceNotFoundException
                        .of("ProductVariant", request.variantId()));

        // 2. Check stock availability
        if (!catalogModuleApi.isVariantAvailable(request.variantId(), request.quantity())) {
            throw new DomainException(
                    "Insufficient stock for: " + variant.displayName(),
                    HttpStatus.CONFLICT);
        }

        // 3. Increment if variant already in cart
        Optional<CartItem> existing = cart.findItemByVariantId(request.variantId());
        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + request.quantity();
            if (newQty > MAX_QTY_PER_ITEM) {
                throw new ValidationException(
                        "Cannot add more than " + MAX_QTY_PER_ITEM + " of the same item");
            }
            if (!catalogModuleApi.isVariantAvailable(request.variantId(), newQty)) {
                throw new DomainException(
                        "Only " + variant.stockQty() + " units available for: "
                                + variant.displayName(),
                        HttpStatus.CONFLICT);
            }
            existing.get().incrementQuantity(request.quantity());
            return;
        }

        // 4. Cart capacity check
        if (cart.getItems().size() >= MAX_DISTINCT_ITEMS) {
            throw new ValidationException(
                    "Your cart is full. Maximum " + MAX_DISTINCT_ITEMS + " different items allowed.");
        }

        // 5. Fetch product info for snapshot
        ProductSummary product = catalogModuleApi.getProductById(variant.productId())
                .orElseThrow(() -> ResourceNotFoundException
                        .of("Product", variant.productId()));

        CartItem newItem = CartItem.builder()
                .productId(variant.productId())
                .variantId(variant.id())
                .productName(product.name())
                .variantName(variant.displayName())
                .sku(variant.sku())
                .unitPrice(variant.price())
                .currencyCode(variant.currencyCode())
                .imageUrl(product.primaryImageUrl())
                .quantity(request.quantity())
                .build();

        cart.addItem(newItem);
    }

    private Cart resolveUserCart(UUID userId) {
        List<Cart> carts = cartRepository.findActiveByUserId(userId);
        return carts.isEmpty()
                ? Cart.builder().userId(userId).build()
                : carts.get(0);
    }

    private Cart resolveOrCreateUserCart(UUID userId) {
        List<Cart> carts = cartRepository.findActiveByUserId(userId);
        if (!carts.isEmpty()) return carts.get(0);
        return cartRepository.save(Cart.builder().userId(userId).build());
    }
}