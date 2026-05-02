package in.sipora.backend.modules.review.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * All request and response records for the review module web layer.
 */
public final class ReviewDTOs {

    private ReviewDTOs() {}

    // Requests ==>

    public record PostReviewRequest(
            @NotNull(message = "Rating is required")
            @Min(value = 1, message = "Rating must be at least 1")
            @Max(value = 5, message = "Rating cannot exceed 5")
            Integer rating,

            @Size(max = 150, message = "Title must be under 150 characters")
            String title,

            @NotBlank(message = "Review body is required")
            @Size(min = 10,  message = "Review must be at least 10 characters")
            @Size(max = 2000, message = "Review must be under 2000 characters")
            String body
    ) {}

    // Responses ==>

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReviewResponse(
            UUID reviewId,
            UUID productId,
            UUID userId,
            String userName,
            int rating,
            String title,
            String body,
            boolean verifiedPurchase,
            int helpfulVotes,
            boolean hidden,
            Instant createdAt
    ) {}

    /**
     * Aggregated rating stats shown on the product detail page.
     *
     * Example:
     * {
     *   "averageRating": 4.3,
     *   "totalReviews": 127,
     *   "distribution": { "5": 72, "4": 31, "3": 14, "2": 6, "1": 4 }
     * }
     */
    public record ReviewStatsResponse(
            double averageRating,
            long totalReviews,
            Map<String, Long> distribution   // "5" → 72, "4" → 31, etc.
    ) {}
}