package in.sipora.backend.modules.ordering.web;

import in.sipora.backend.modules.identity.api.IdentityModuleApi;
import in.sipora.backend.modules.identity.api.UserSummary;
import in.sipora.backend.modules.ordering.application.OrderService;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.CancelOrderRequest;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderCardResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.PlaceOrderRequest;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.PlaceOrderResponse;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.util.SecurityUtils;
import in.sipora.backend.shared.web.ApiResponse;
import in.sipora.backend.shared.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Place, view and cancel orders")
public class OrderController {

    private final OrderService orderService;
    private final IdentityModuleApi identityModuleApi;

    @PostMapping
    @Operation(summary = "Place a new order from the active cart")
    public ResponseEntity<ApiResponse<PlaceOrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        UserSummary user = identityModuleApi.getUserById(userId)
                .orElseThrow(() -> new DomainException("User not found", HttpStatus.UNAUTHORIZED));

        PlaceOrderResponse response = orderService.placeOrder(
                userId, request, user.email(), user.fullName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully. Proceed to payment.", response));
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's order history")
    public ResponseEntity<ApiResponse<PageResponse<OrderCardResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getMyOrders(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get full detail of a specific order")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrder(
            @PathVariable UUID orderId) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved",
                        orderService.getMyOrder(userId, orderId)));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order — only possible before it ships")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        CancelOrderRequest req = request != null ? request : new CancelOrderRequest(null);
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled",
                        orderService.cancelMyOrder(userId, orderId, req)));
    }
}