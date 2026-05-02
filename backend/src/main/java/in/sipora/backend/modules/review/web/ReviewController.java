package in.sipora.backend.modules.review.web;

import in.sipora.backend.modules.review.application.ReviewService;
import in.sipora.backend.modules.review.web.ReviewDTOs.PostReviewRequest;
import in.sipora.backend.modules.review.web.ReviewDTOs.ReviewResponse;
import in.sipora.backend.modules.review.web.ReviewDTOs.ReviewStatsResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Review REST endpoints.
 *
 * URL structure follows the resource hierarchy:
 *   /api/v1/products/{productId}/reviews — product-scoped reviews
 *   /api/v1/users/me/reviews — user's own reviews
 *   /api/v1/admin/reviews/{reviewId} — admin moderation
 *
 * Public endpoints (no auth):
 *   GET  /products/{productId}/reviews — paginated review list
 *   GET  /products/{productId}/reviews/stats — avg + distribution
 *
 * Auth required:
 *   POST /products/{productId}/reviews — post review (verified buyer only)
 *   POST /products/{productId}/reviews/{id}/helpful — upvote
 *   DELETE /products/{productId}/reviews/{id} — delete own review
 *   GET  /users/me/reviews — my reviews
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews — verified buyers only")
public class ReviewController {

    private final ReviewService reviewService;

    // Storefront — public ==>

    @GetMapping("/api/v1/products/{productId}/reviews")
    @Operation(summary = "List visible reviews for a product")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return ResponseEntity.ok(ApiResponse.success(
                "Reviews retrieved",
                reviewService.getProductReviews(productId,
                        PageRequest.of(page, Math.min(size, 50), sort))));
    }

    @GetMapping("/api/v1/products/{productId}/reviews/stats")
    @Operation(summary = "Get average rating and star distribution for a product")
    public ResponseEntity<ApiResponse<ReviewStatsResponse>> getProductReviewStats(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Review stats retrieved",
                reviewService.getProductReviewStats(productId)));
    }

    // Storefront — auth required ==>

    @PostMapping("/api/v1/products/{productId}/reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Post a review for a product",
            description = "Requires a DELIVERED order containing this product. " +
                    "One review per user per product."
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> postReview(
            @PathVariable UUID productId,
            @Valid @RequestBody PostReviewRequest request) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        ReviewResponse response = reviewService.postReview(userId, productId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review posted", response));
    }

    @DeleteMapping("/api/v1/products/{productId}/reviews/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete your own review (allows you to re-post a new one)")
    public ResponseEntity<ApiResponse<Void>> deleteMyReview(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {

        reviewService.deleteMyReview(SecurityUtils.requireCurrentUserId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    @PostMapping("/api/v1/products/{productId}/reviews/{reviewId}/helpful")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark a review as helpful")
    public ResponseEntity<ApiResponse<ReviewResponse>> markHelpful(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Marked as helpful", reviewService.markHelpful(reviewId, userId)));
    }

    // User — own review history ==>

    @GetMapping("/api/v1/users/me/reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all reviews posted by the authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Reviews retrieved",
                reviewService.getMyReviews(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    // Admin — moderation ==>

    @GetMapping("/api/v1/admin/products/{productId}/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] List all reviews including hidden ones")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> adminGetProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Reviews retrieved",
                reviewService.adminGetProductReviews(productId,
                        PageRequest.of(page, size))));
    }

    @DeleteMapping("/api/v1/admin/reviews/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Permanently delete a review")
    public ResponseEntity<ApiResponse<Void>> adminDeleteReview(@PathVariable UUID reviewId) {
        reviewService.adminDeleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    @PatchMapping("/api/v1/admin/reviews/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Soft-hide a review (keeps DB record, removes from storefront)")
    public ResponseEntity<ApiResponse<ReviewResponse>> adminHideReview(
            @PathVariable UUID reviewId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Review hidden", reviewService.adminHideReview(reviewId)));
    }

    @PatchMapping("/api/v1/admin/reviews/{reviewId}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "[ADMIN] Restore a previously hidden review")
    public ResponseEntity<ApiResponse<ReviewResponse>> adminUnhideReview(
            @PathVariable UUID reviewId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Review restored", reviewService.adminUnhideReview(reviewId)));
    }
}