package in.sipora.backend.modules.ordering.application;

import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.ordering.api.OrderSummary;
import in.sipora.backend.modules.ordering.api.OrderingModuleApi;
import in.sipora.backend.modules.ordering.domain.Order;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderCancelledEvent;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderConfirmedEvent;
import in.sipora.backend.modules.ordering.domain.OrderRepository;
import in.sipora.backend.modules.ordering.domain.OrderStatus;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the public ordering contract consumed by payment and review modules.
 *
 * confirmOrder and cancelOrder are called inside the payment module's
 * webhook handler — they participate in the same transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderingModuleApiImpl implements OrderingModuleApi {

    private final OrderRepository orderRepository;
    private final CatalogModuleApi catalogModuleApi;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderSummary> getOrderById(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .map(orderMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderSummary> getOrderByGatewayOrderId(String gatewayOrderId) {
        return orderRepository.findByGatewayOrderId(gatewayOrderId)
                .map(orderMapper::toSummary);
    }

    @Override
    @Transactional
    public void confirmOrder(UUID orderId) {
        Order order = findWithItemsOrThrow(orderId);
        order.transition(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order confirmed: {}", order.getOrderNumber());

        eventPublisher.publishEvent(new OrderConfirmedEvent(
                order.getId(), order.getOrderNumber(), order.getUserId(),
                order.getTotalAmount(), order.getCurrencyCode(),
                "email-placeholder", "name-placeholder"));
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        Order order = findWithItemsOrThrow(orderId);

        if (!order.isCancellable()) {
            throw new DomainException(
                    "Cannot cancel order in status: " + order.getStatus(), HttpStatus.CONFLICT);
        }

        order.transition(OrderStatus.CANCELLED);
        order.setCancelReason(reason);

        // Restore stock for each item
        order.getItems().forEach(item ->
                catalogModuleApi.restoreStock(item.getVariantId(), item.getQuantity()));

        orderRepository.save(order);
        log.info("Order cancelled: {} reason={}", order.getOrderNumber(), reason);

        eventPublisher.publishEvent(new OrderCancelledEvent(
                order.getId(), order.getOrderNumber(), order.getUserId(),
                reason, "email-placeholder", "name-placeholder"));
    }

    @Override
    @Transactional
    public void setGatewayOrderId(UUID orderId, String gatewayOrderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
        order.setGatewayOrderId(gatewayOrderId);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(UUID userId, UUID productId) {
        return orderRepository.existsDeliveredOrderForUserAndProduct(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummary> getOrdersByUserId(UUID userId) {
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(orderMapper::toSummary)
                .toList();
    }

    private Order findWithItemsOrThrow(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }
}