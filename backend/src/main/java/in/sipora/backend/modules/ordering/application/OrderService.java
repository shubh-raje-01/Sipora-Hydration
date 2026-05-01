package in.sipora.backend.modules.ordering.application;

import in.sipora.backend.modules.cart.api.CartItemSummary;
import in.sipora.backend.modules.cart.api.CartModuleApi;
import in.sipora.backend.modules.cart.api.CartSummary;
import in.sipora.backend.modules.catalog.api.CatalogModuleApi;
import in.sipora.backend.modules.ordering.domain.Order;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderCancelledEvent;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderConfirmedEvent;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderPlacedEvent;
import in.sipora.backend.modules.ordering.domain.OrderItem;
import in.sipora.backend.modules.ordering.domain.OrderRepository;
import in.sipora.backend.modules.ordering.domain.OrderStatus;
import in.sipora.backend.modules.ordering.domain.ShippingAddress;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.CancelOrderRequest;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderCardResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.PlaceOrderRequest;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.PlaceOrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.UpdateOrderStatusRequest;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;
import in.sipora.backend.shared.web.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core ordering service.
 *
 * placeOrder() is the most critical method — it runs in a single @Transactional
 * boundary that covers:
 *   1. Cart validation
 *   2. Stock deduction (catalog module — SELECT FOR UPDATE)
 *   3. Order + OrderItems persistence
 *   4. Cart clearance
 *
 * If any step throws, the entire transaction rolls back:
 *   - Stock is never deducted without a saved order
 *   - Cart is never cleared without stock being deducted
 *
 * Spring Events (OrderPlacedEvent etc.) are published AFTER commit via
 *   @TransactionalEventListener(phase = AFTER_COMMIT)
 * — but we publish with the standard publisher here; the notification module's
 * listener should be annotated with @TransactionalEventListener(phase = AFTER_COMMIT)
 * to ensure emails only fire after the DB commit succeeds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("499");
    private static final BigDecimal SHIPPING_CHARGE = new BigDecimal("49");

    private final OrderRepository orderRepository;
    private final CartModuleApi cartModuleApi;
    private final CatalogModuleApi catalogModuleApi;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ── Place order ==>

    /**
     * Places a new order from the user's active cart.
     *
     * Returns a PlaceOrderResponse with the orderId so the frontend can
     * immediately call POST /api/v1/payments/create-order to initiate payment.
     */
    @Transactional
    public PlaceOrderResponse placeOrder(UUID userId, PlaceOrderRequest request,
                                         String userEmail, String userName) {

        // 1 — Validate cart has items
        CartSummary cart = cartModuleApi.getCartForUser(userId)
                .orElseThrow(() -> new ValidationException(
                        "Your cart is empty. Add items before placing an order."));

        if (cart.isEmpty()) {
            throw new ValidationException("Your cart is empty.");
        }

        // 2 — Build order
        BigDecimal subtotal = cart.subtotal();
        BigDecimal shippingCharge = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO : SHIPPING_CHARGE;
        BigDecimal totalAmount = subtotal.add(shippingCharge);

        Order order = Order.builder()
                .orderNumber(orderNumberGenerator.next())
                .userId(userId)
                .cartId(cart.cartId())
                .subtotal(subtotal)
                .shippingCharge(shippingCharge)
                .totalAmount(totalAmount)
                .shippingAddress(buildShippingAddress(request))
                .build();

        // 3 — Create order items from cart snapshots + deduct stock atomically
        List<UUID> deductedVariants = new ArrayList<>();
        try {
            for (CartItemSummary cartItem : cart.items()) {
                catalogModuleApi.deductStock(cartItem.variantId(), cartItem.quantity());
                deductedVariants.add(cartItem.variantId());

                order.addItem(OrderItem.builder()
                        .productId(cartItem.productId())
                        .variantId(cartItem.variantId())
                        .productName(cartItem.productName())
                        .variantName(cartItem.variantName())
                        .sku(cartItem.sku())
                        .unitPrice(cartItem.unitPrice())
                        .currencyCode(cartItem.currencyCode())
                        .imageUrl(null)
                        .quantity(cartItem.quantity())
                        .build());
            }
        } catch (Exception ex) {
            // Rollback any stock already deducted (should not be needed with @Transactional,
            // but explicit for clarity in case of partial failure in loop)
            log.error("Stock deduction failed mid-order for userId={}. Rolling back.", userId, ex);
            throw ex; // Let @Transactional handle the rollback
        }

        // 4 — Persist order
        Order saved = orderRepository.save(order);

        // 5 — Clear cart (inside the same transaction)
        cartModuleApi.clearCart(userId, cart.cartId());

        log.info("Order placed: {} for userId={} total=INR {}", saved.getOrderNumber(), userId, totalAmount);

        // 6 — Publish event (notification listener fires after commit)
        eventPublisher.publishEvent(new OrderPlacedEvent(
                saved.getId(), saved.getOrderNumber(), userId,
                totalAmount, "INR", userEmail, userName));

        return orderMapper.toPlaceOrderResponse(saved);
    }

    // ── User order history ==>

    @Transactional(readOnly = true)
    public PageResponse<OrderCardResponse> getMyOrders(UUID userId, Pageable pageable) {
        return PageResponse.from(
                orderRepository.findByUserId(userId, pageable)
                        .map(orderMapper::toCardResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(UUID userId, UUID orderId) {
        Order order = findWithItemsOrThrow(orderId);
        if (!order.belongsTo(userId)) {
            throw new DomainException("Order not found", HttpStatus.NOT_FOUND);
        }
        return orderMapper.toResponse(order);
    }

    // ── Cancel ==>

    @Transactional
    public OrderResponse cancelMyOrder(UUID userId, UUID orderId, CancelOrderRequest request) {
        Order order = findWithItemsOrThrow(orderId);

        if (!order.belongsTo(userId)) {
            throw new DomainException("Order not found", HttpStatus.NOT_FOUND);
        }
        if (!order.isCancellable()) {
            throw new DomainException(
                    "Orders in status " + order.getStatus() + " cannot be cancelled",
                    HttpStatus.CONFLICT);
        }

        return performCancellation(order,
                request.reason() != null ? request.reason() : "Cancelled by customer",
                "email-placeholder", "name-placeholder");
    }

    // ── Admin operations ==>

    @Transactional(readOnly = true)
    public PageResponse<OrderCardResponse> getAllOrders(String statusFilter, Pageable pageable) {
        OrderStatus status = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try { status = OrderStatus.valueOf(statusFilter.toUpperCase()); }
            catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid order status: " + statusFilter);
            }
        }
        return PageResponse.from(
                orderRepository.findAllForAdmin(status, pageable)
                        .map(orderMapper::toCardResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return orderMapper.toResponse(findWithItemsOrThrow(orderId));
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = findWithItemsOrThrow(orderId);
        OrderStatus next;
        try {
            next = OrderStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + request.status());
        }

        order.transition(next);

        if (request.trackingNumber() != null && !request.trackingNumber().isBlank()) {
            order.setTrackingNumber(request.trackingNumber());
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse adminCancelOrder(UUID orderId, CancelOrderRequest request) {
        Order order = findWithItemsOrThrow(orderId);
        if (!order.isCancellable()) {
            throw new DomainException(
                    "Cannot cancel order in status: " + order.getStatus(), HttpStatus.CONFLICT);
        }
        return performCancellation(order,
                request.reason() != null ? request.reason() : "Cancelled by admin",
                "email-placeholder", "name-placeholder");
    }

    // ── Internal ==>

    /**
     * Shared cancellation logic used by both user self-cancel and admin cancel.
     * Transitions status, restores stock, publishes event.
     */
    private OrderResponse performCancellation(Order order, String reason,
                                              String userEmail, String userName) {
        order.transition(OrderStatus.CANCELLED);
        order.setCancelReason(reason);

        // Restore stock for every line item
        for (OrderItem item : order.getItems()) {
            catalogModuleApi.restoreStock(item.getVariantId(), item.getQuantity());
        }

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: {} reason={}", saved.getOrderNumber(), reason);

        eventPublisher.publishEvent(new OrderCancelledEvent(
                saved.getId(), saved.getOrderNumber(),
                saved.getUserId(), reason, userEmail, userName));

        return orderMapper.toResponse(saved);
    }

    private Order findWithItemsOrThrow(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    private ShippingAddress buildShippingAddress(PlaceOrderRequest request) {
        var a = request.shippingAddress();
        return ShippingAddress.builder()
                .fullName(a.fullName().trim())
                .phone(a.phone())
                .line1(a.line1().trim())
                .line2(a.line2())
                .city(a.city().trim())
                .state(a.state().trim())
                .pinCode(a.pinCode())
                .build();
    }
}