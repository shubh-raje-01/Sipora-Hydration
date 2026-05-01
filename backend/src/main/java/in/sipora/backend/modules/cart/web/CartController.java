package in.sipora.backend.modules.cart.web;

import in.sipora.backend.modules.cart.application.CartService;
import in.sipora.backend.modules.cart.web.CartDTOs.AddItemRequest;
import in.sipora.backend.modules.cart.web.CartDTOs.CartResponse;
import in.sipora.backend.modules.cart.web.CartDTOs.MergeCartRequest;
import in.sipora.backend.modules.cart.web.CartDTOs.UpdateItemRequest;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.util.SecurityUtils;
import in.sipora.backend.shared.web.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * Cart REST endpoints.
 *
 * All endpoints accept both authenticated and guest requests.
 * Guest identity is carried by the X-Session-Id header — a UUID the
 * React frontend generates once and stores in localStorage.
 *
 * Rules:
 *  - Authenticated user  → operations apply to their user cart (userId-keyed)
 *  - Guest               → operations apply to their session cart (sessionId-keyed)
 *  - POST /merge         → called by the frontend immediately after login to
 *                          combine the guest cart into the user cart
 *
 * Update and remove operations always require authentication to prevent
 * one guest from accidentally modifying another's cart via a guessed sessionId.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart — supports guest and authenticated users")
public class CartController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final CartService cartService;

    // ── Get cart ==>

    @GetMapping
    @Operation(summary = "Get the current cart — works for both guest and authenticated users")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(description = "Guest session UUID from localStorage")
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionId) {

        Optional<UUID> userId = SecurityUtils.getCurrentUserId();

        CartResponse cart = userId.isPresent()
                ? cartService.getCartForUser(userId.get())
                : cartService.getGuestCart(requireSessionId(sessionId));

        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }

    // ── Add item ==>

    @PostMapping("/items")
    @Operation(summary = "Add a variant to the cart — works for guest and authenticated users")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionId,
            @Valid @RequestBody AddItemRequest request) {

        Optional<UUID> userId = SecurityUtils.getCurrentUserId();

        CartResponse cart = userId.isPresent()
                ? cartService.addItemForUser(userId.get(), request)
                : cartService.addItemForGuest(requireSessionId(sessionId), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", cart));
    }

    // ── Update item ==>

    @PatchMapping("/items/{cartItemId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update quantity of a cart item (auth required)")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateItemRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Cart updated",
                        cartService.updateItem(userId, cartItemId, request)));
    }

    // ── Remove item ==>

    @DeleteMapping("/items/{cartItemId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove an item from the cart (auth required)")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable UUID cartItemId) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Item removed",
                        cartService.removeItem(userId, cartItemId)));
    }

    // ── Clear cart ==>

    @DeleteMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Clear all items from the cart (auth required)")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCartForUser(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }

    // ── Merge on login ==>

    @PostMapping("/merge")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Merge guest cart into user cart after login",
            description = "Call this immediately after a successful login if the user had a guest cart. " +
                    "Pass the sessionId that was used during guest browsing."
    )
    public ResponseEntity<ApiResponse<CartResponse>> mergeCarts(
            @Valid @RequestBody MergeCartRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Cart merged",
                        cartService.mergeCarts(userId, request)));
    }

    // ── Helper ==>

    private String requireSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new DomainException(
                    "Either authenticate or provide X-Session-Id header for guest cart access",
                    HttpStatus.BAD_REQUEST);
        }
        return sessionId;
    }
}