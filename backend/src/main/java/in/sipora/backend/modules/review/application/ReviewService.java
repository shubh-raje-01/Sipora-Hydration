package in.sipora.backend.modules.review.application;

import in.sipora.backend.modules.identity.api.IdentityModuleApi;
import in.sipora.backend.modules.identity.api.UserSummary;
import in.sipora.backend.modules.ordering.api.OrderingModuleApi;
import in.sipora.backend.modules.review.domain.Review;
import in.sipora.backend.modules.review.domain.ReviewRepository;
import in.sipora.backend.modules.review.web.ReviewDTOs.PostReviewRequest;
import in.sipora.backend.modules.review.web.ReviewDTOs.ReviewResponse;
import in.sipora.backend.modules.review.web.ReviewDTOs.ReviewStatsResponse;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;
import in.sipora.backend.shared.web.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Review service.
 *
 * Verified-purchase gate:
 *  Before allowing a review, checks OrderingModuleApi.hasUserPurchasedProduct().
 *  This returns true only if the user has a DELIVERED order containing the product.
 *  This prevents fake reviews from accounts that never bought the product.
 *
 * Cross-module calls:
 *  - IdentityModuleApi → fetch user name for snapshot
 *  - OrderingModuleApi → verify purchase before posting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderingModuleApi orderingModuleApi;
    private final IdentityModuleApi identityModuleApi;
    private final ReviewMapper reviewMapper;

    // ── Post review ==>

    /**
     * Posts a review after verifying the user has purchased and received the product.
     *
     * Checks performed:
     *  1. User exists and is active (IdentityModuleApi)
     *  2. User has a DELIVERED order containing this product (OrderingModuleApi)
     *  3. User has not already reviewed this product (DB unique constraint)
     */
    @Transactional
    public ReviewResponse postReview(UUID userId, UUID productId, PostReviewRequest request) {

        // 1 — Fetch user name for snapshot
        UserSummary user = identityModuleApi.getUserById(userId)
                .orElseThrow(() -> new DomainException("User not found", HttpStatus.UNAUTHORIZED));

        // 2 — Verified purchase gate
        if (!orderingModuleApi.hasUserPurchasedProduct(userId, productId)) {
            throw new DomainException(
                    "You can only review products you have purchased and received. " +
                            "This product is not in any of your delivered orders.",
                    HttpStatus.FORBIDDEN);
        }

        // 3 — One review per user per product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new ValidationException(
                    "You have already reviewed this product. " +
                            "Please delete your existing review before posting a new one.");
        }

        Review review = Review.builder()
                .productId(productId)
                .userId(userId)
                .userName(user.fullName())
                .rating(request.rating())
                .title(request.title())
                .body(request.body().trim())
                .verifiedPurchase(true)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review posted: productId={} userId={} rating={}", productId, userId, request.rating());
        return reviewMapper.toResponse(saved);
    }

    // ── Read ==>

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(UUID productId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findVisibleByProductId(productId, pageable)
                        .map(reviewMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ReviewStatsResponse getProductReviewStats(UUID productId) {
        Object[] stats = reviewRepository.findRatingStatsByProductId(productId);
        List<Object[]> dist = reviewRepository.findRatingDistributionByProductId(productId);

        double average    = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
        long   totalCount = stats[1] != null ? ((Number) stats[1]).longValue()   : 0L;

        // Build distribution map — initialise all stars to 0 then fill from query
        Map<String, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(String.valueOf(i), 0L);
        for (Object[] row : dist) {
            String star = String.valueOf(((Number) row[0]).intValue());
            long   count = ((Number) row[1]).longValue();
            distribution.put(star, count);
        }

        // Round average to 1 decimal place
        double rounded = Math.round(average * 10.0) / 10.0;
        return new ReviewStatsResponse(rounded, totalCount, distribution);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(UUID userId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findByUserId(userId, pageable)
                        .map(reviewMapper::toResponse));
    }

    // ── Delete ==>

    /**
     * Users can delete their own review.
     * Hard delete — allows them to post a replacement review afterwards.
     */
    @Transactional
    public void deleteMyReview(UUID userId, UUID reviewId) {
        Review review = findOrThrow(reviewId);

        if (!review.belongsTo(userId)) {
            throw new DomainException("Review not found", HttpStatus.NOT_FOUND);
        }

        reviewRepository.delete(review);
        log.info("Review deleted by owner: reviewId={} userId={}", reviewId, userId);
    }

    /**
     * Admin hard-deletes a review (removes it entirely from the database).
     * Use hideReview() for a softer option that keeps the audit trail.
     */
    @Transactional
    public void adminDeleteReview(UUID reviewId) {
        Review review = findOrThrow(reviewId);
        reviewRepository.delete(review);
        log.info("Review deleted by admin: reviewId={}", reviewId);
    }

    /**
     * Admin soft-hides a review. Stays in DB for audit, disappears from storefront.
     * Preferred over hard delete for policy violations (can be reinstated).
     */
    @Transactional
    public ReviewResponse adminHideReview(UUID reviewId) {
        Review review = findOrThrow(reviewId);

        if (review.isHidden()) {
            throw new ValidationException("Review is already hidden");
        }

        review.hide();
        Review saved = reviewRepository.save(review);
        log.info("Review hidden by admin: reviewId={}", reviewId);
        return reviewMapper.toResponse(saved);
    }

    /** Admin unhides a previously hidden review. */
    @Transactional
    public ReviewResponse adminUnhideReview(UUID reviewId) {
        Review review = findOrThrow(reviewId);
        review.setHidden(false);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    // ── Helpful votes ==>

    /**
     * Increments the "Was this review helpful?" counter.
     *
     * Simple increment — no per-user deduplication at the DB level.
     * For production, add a separate review_helpful_votes junction table
     * with (review_id, user_id) unique constraint to prevent ballot stuffing.
     */
    @Transactional
    public ReviewResponse markHelpful(UUID reviewId, UUID userId) {
        Review review = findOrThrow(reviewId);

        if (review.belongsTo(userId)) {
            throw new ValidationException("You cannot mark your own review as helpful");
        }
        if (review.isHidden()) {
            throw new ResourceNotFoundException("Review not found: " + reviewId);
        }

        review.incrementHelpfulVotes();
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    // ── Admin listing ==>

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> adminGetProductReviews(UUID productId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findAllByProductIdForAdmin(productId, pageable)
                        .map(reviewMapper::toResponse));
    }

    // ── Internal ==>

    private Review findOrThrow(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> ResourceNotFoundException.of("Review", reviewId));
    }
}