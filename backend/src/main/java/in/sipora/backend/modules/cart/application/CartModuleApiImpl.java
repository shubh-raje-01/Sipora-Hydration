package in.sipora.backend.modules.cart.application;

import in.sipora.backend.modules.cart.api.CartModuleApi;
import in.sipora.backend.modules.cart.api.CartSummary;
import in.sipora.backend.modules.cart.domain.Cart;
import in.sipora.backend.modules.cart.domain.CartItemRepository;
import in.sipora.backend.modules.cart.domain.CartRepository;
import in.sipora.backend.modules.cart.domain.CartStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the public cart contract consumed by the ordering module.
 *
 * OrderService calls:
 *  1. getCartForUser()  — to snapshot line items and totals
 *  2. clearCart()       — after order is successfully persisted
 *
 * Both run inside the ordering module's @Transactional boundary,
 * ensuring the cart is only cleared if the order insert also succeeds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartModuleApiImpl implements CartModuleApi {

    private final CartRepository     cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper         cartMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<CartSummary> getCartForUser(UUID userId) {
        List<Cart> carts = cartRepository.findActiveByUserId(userId);
        if (carts.isEmpty() || carts.get(0).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cartMapper.toSummary(carts.get(0)));
    }

    @Override
    @Transactional
    public void clearCart(UUID userId, UUID cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            if (!cart.getUserId().equals(userId)) {
                log.warn("clearCart called with mismatched userId/cartId: userId={} cartId={}",
                        userId, cartId);
                return;
            }
            cartItemRepository.deleteAllByCartId(cartId);
            cart.setStatus(CartStatus.CHECKED_OUT);
            cartRepository.save(cart);
            log.info("Cart cleared: cartId={} userId={}", cartId, userId);
        });
    }
}