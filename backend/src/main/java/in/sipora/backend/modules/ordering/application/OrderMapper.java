package in.sipora.backend.modules.ordering.application;

import in.sipora.backend.modules.ordering.api.OrderSummary;
import in.sipora.backend.modules.ordering.domain.Order;
import in.sipora.backend.modules.ordering.domain.OrderItem;
import in.sipora.backend.modules.ordering.domain.ShippingAddress;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderCardResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderItemResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.PlaceOrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.ShippingAddressResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(this::toItemResponse).toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getSubtotal(),
                order.getShippingCharge(),
                order.getTotalAmount(),
                order.getCurrencyCode(),
                toAddressResponse(order.getShippingAddress()),
                order.getTrackingNumber(),
                order.getCancelReason(),
                order.getCreatedAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt(),
                itemResponses
        );
    }

    public OrderCardResponse toCardResponse(Order order) {
        int itemCount = order.getItems() == null ? 0
                : order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
        return new OrderCardResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                itemCount,
                order.getTotalAmount(),
                order.getCurrencyCode(),
                order.getCreatedAt()
        );
    }

    public PlaceOrderResponse toPlaceOrderResponse(Order order) {
        return new PlaceOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCurrencyCode()
        );
    }

    public OrderSummary toSummary(Order order) {
        List<java.util.UUID> productIds = order.getItems() == null ? List.of()
                : order.getItems().stream().map(OrderItem::getProductId).distinct().toList();
        List<java.util.UUID> variantIds = order.getItems() == null ? List.of()
                : order.getItems().stream().map(OrderItem::getVariantId).toList();

        return new OrderSummary(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getSubtotal(),
                order.getShippingCharge(),
                order.getCurrencyCode(),
                productIds,
                variantIds,
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
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

    private ShippingAddressResponse toAddressResponse(ShippingAddress a) {
        if (a == null) return null;
        return new ShippingAddressResponse(
                a.getFullName(), a.getPhone(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPinCode(), a.getCountry()
        );
    }
}