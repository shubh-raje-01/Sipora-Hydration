package in.sipora.backend.modules.ordering.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * All request and response records for the ordering module web layer.
 */
public final class OrderingDTOs {

    private OrderingDTOs() {}

    // Requests ==>

    public record PlaceOrderRequest(
            @NotNull(message = "Shipping address is required")
            @Valid
            ShippingAddressRequest shippingAddress
    ) {}

    public record ShippingAddressRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 150)
            String fullName,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "^[6-9]\\d{9}$", message = "Must be a valid 10-digit Indian mobile number")
            String phone,

            @NotBlank(message = "Address line 1 is required")
            @Size(max = 255)
            String line1,

            @Size(max = 255)
            String line2,

            @NotBlank(message = "City is required")
            @Size(max = 100)
            String city,

            @NotBlank(message = "State is required")
            @Size(max = 100)
            String state,

            @NotBlank(message = "PIN code is required")
            @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Must be a valid 6-digit Indian PIN code")
            String pinCode
    ) {}

    public record CancelOrderRequest(
            @Size(max = 500)
            String reason
    ) {}

    /** Admin-only — move order through PROCESSING → SHIPPED → DELIVERED */
    public record UpdateOrderStatusRequest(
            @NotBlank
            String status,

            @Size(max = 100)
            String trackingNumber,

            @Size(max = 500)
            String note
    ) {}

    // Responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderResponse(
            UUID orderId,
            String orderNumber,
            String status,
            BigDecimal subtotal,
            BigDecimal shippingCharge,
            BigDecimal totalAmount,
            String currencyCode,
            ShippingAddressResponse shippingAddress,
            String trackingNumber,
            String cancelReason,
            Instant createdAt,
            Instant shippedAt,
            Instant deliveredAt,
            Instant cancelledAt,
            List<OrderItemResponse> items
    ) {}

    public record OrderItemResponse(
            UUID orderItemId,
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

    public record ShippingAddressResponse(
            String fullName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String pinCode,
            String country
    ) {}

    /** Minimal card used in the order history list — no full item detail. */
    public record OrderCardResponse(
            UUID orderId,
            String orderNumber,
            String status,
            int itemCount,
            BigDecimal totalAmount,
            String currencyCode,
            Instant createdAt
    ) {}

    /**
     * Returned after order placement so the frontend knows the orderId
     * to pass to the payment initiation endpoint.
     */
    public record PlaceOrderResponse(
            UUID orderId,
            String orderNumber,
            String status,
            BigDecimal totalAmount,
            String currencyCode
    ) {}
}
