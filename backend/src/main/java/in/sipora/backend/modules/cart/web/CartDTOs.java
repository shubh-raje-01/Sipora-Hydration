package in.sipora.backend.modules.cart.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * All request/response records for the cart module web layer.
 */
public final class CartDTOs {

    private CartDTOs() {}

    // Requests ==>

    public record AddItemRequest(
            @NotNull(message = "variantId is required")
            UUID variantId,

            @Min(value = 1,  message = "Quantity must be at least 1")
            @Max(value = 20, message = "Cannot add more than 20 of a single item")
            int quantity
    ) {}

    public record UpdateItemRequest(
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 20, message = "Cannot exceed 20 of a single item")
            int quantity
    ) {}

    public record MergeCartRequest(
            @NotNull(message = "sessionId is required")
            String sessionId
    ) {}

    // Responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CartResponse(
            UUID cartId,
            List<CartItemResponse> items,
            int totalItems,
            BigDecimal subtotal,
            String currencyCode
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CartItemResponse(
            UUID cartItemId,
            UUID productId,
            UUID variantId,
            String productName,
            String variantName,
            String sku,
            String imageUrl,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            String currencyCode
    ) {}
}
