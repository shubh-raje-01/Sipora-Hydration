package in.sipora.backend.modules.cart.application;

import in.sipora.backend.modules.cart.api.CartItemSummary;
import in.sipora.backend.modules.cart.api.CartSummary;
import in.sipora.backend.modules.cart.domain.Cart;
import in.sipora.backend.modules.cart.domain.CartItem;
import in.sipora.backend.modules.cart.web.CartDTOs.CartItemResponse;
import in.sipora.backend.modules.cart.web.CartDTOs.CartResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems() == null
                ? List.of()
                : cart.getItems().stream().map(this::toItemResponse).toList();

        return new CartResponse(
                cart.getId(),
                itemResponses,
                cart.getTotalItemCount(),
                cart.getSubtotal(),
                "INR"
        );
    }

    public CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getProductName(),
                item.getVariantName(),
                item.getSku(),
                item.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getCurrencyCode()
        );
    }

    public CartSummary toSummary(Cart cart) {
        List<CartItemSummary> itemSummaries = cart.getItems() == null
                ? List.of()
                : cart.getItems().stream().map(this::toItemSummary).toList();

        return new CartSummary(
                cart.getId(),
                cart.getUserId(),
                itemSummaries,
                cart.getTotalItemCount(),
                cart.getSubtotal(),
                "INR"
        );
    }

    public CartItemSummary toItemSummary(CartItem item) {
        return new CartItemSummary(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                item.getProductName(),
                item.getVariantName(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getCurrencyCode()
        );
    }
}