package in.sipora.backend.modules.ordering.web;

import in.sipora.backend.modules.ordering.application.OrderService;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.CancelOrderRequest;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderCardResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.OrderResponse;
import in.sipora.backend.modules.ordering.web.OrderingDTOs.UpdateOrderStatusRequest;
import in.sipora.backend.shared.web.ApiResponse;
import in.sipora.backend.shared.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin — Orders", description = "Admin order management and fulfilment")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List all orders with optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<OrderCardResponse>>> getAllOrders(
            @RequestParam(required = false)    String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getAllOrders(status,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get full order detail by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved", orderService.getOrderById(orderId)));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status — CONFIRMED → PROCESSING → SHIPPED → DELIVERED")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        orderService.updateOrderStatus(orderId, request)));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Admin cancels an order and restores stock")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {

        CancelOrderRequest req = request != null ? request : new CancelOrderRequest(null);
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled",
                        orderService.adminCancelOrder(orderId, req)));
    }
}