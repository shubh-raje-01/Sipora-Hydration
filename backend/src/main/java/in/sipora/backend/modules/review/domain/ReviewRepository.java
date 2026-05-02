package in.sipora.backend.modules.review.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /** Paginated visible reviews for a product — newest first. */
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.hidden = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findVisibleByProductId(@Param("productId") UUID productId, Pageable pageable);

    /** Single review by this user for this product — for duplicate check + owner fetch. */
    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    /** True if the user has already reviewed this product. */
    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    /**
     * Aggregated stats — average rating and total count.
     * Returns Object[] { Double avgRating, Long totalCount }.
     */
    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r " +
            "WHERE r.productId = :productId AND r.hidden = false")
    Object[] findRatingStatsByProductId(@Param("productId") UUID productId);

    /**
     * Rating distribution — count per star (1–5).
     * Returns List<Object[]> where each row is { Integer rating, Long count }.
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.productId = :productId AND r.hidden = false " +
            "GROUP BY r.rating ORDER BY r.rating DESC")
    java.util.List<Object[]> findRatingDistributionByProductId(@Param("productId") UUID productId);

    /** All reviews by a specific user — for user profile page. */
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.hidden = false " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /** Admin — all reviews including hidden ones. */
    @Query("SELECT r FROM Review r WHERE r.productId = :productId " +
            "ORDER BY r.hidden ASC, r.createdAt DESC")
    Page<Review> findAllByProductIdForAdmin(@Param("productId") UUID productId, Pageable pageable);
}