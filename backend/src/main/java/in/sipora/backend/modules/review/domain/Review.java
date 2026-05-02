package in.sipora.backend.modules.review.domain;

import in.sipora.backend.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A product review left by a verified buyer.
 *
 * Business rules enforced in ReviewService:
 *  - Only users with a DELIVERED order containing this product may post
 *  - One review per user per product (DB unique constraint)
 *  - Reviews are immutable after posting (no edit endpoint) — prevents
 *    manipulation. Admins can delete; users must delete and re-post.
 *
 * Cross-module notes:
 *  - productId → plain UUID, no FK to catalog.products
 *  - userId → plain UUID, no FK to identity.users
 *  - userName → snapshotted from IdentityModuleApi at post time so the
 *                display name survives if the user changes their name later
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                name  = "uq_review_product_user",
                columnNames = {"product_id", "user_id"}
        )
)
public class Review extends BaseEntity {

    /** Plain UUID — no FK to catalog.products (cross-module boundary). */
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    /** Plain UUID — no FK to identity.users (cross-module boundary). */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** Snapshotted from IdentityModuleApi at post time. */
    @Column(name = "user_name", nullable = false, updatable = false, length = 150)
    private String userName;

    @Column(name = "rating", nullable = false)
    private int rating;                // 1–5

    @Column(name = "title", length = 150)
    private String title;              // optional short headline

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /** True — all reviews in this system come from verified buyers. */
    @Column(name = "verified_purchase", nullable = false, updatable = false)
    @Builder.Default
    private boolean verifiedPurchase = true;

    /** Running count of "Was this helpful?" upvotes. */
    @Column(name = "helpful_votes", nullable = false)
    @Builder.Default
    private int helpfulVotes = 0;

    /** Soft-delete flag — admin hides review without losing audit trail. */
    @Column(name = "hidden", nullable = false)
    @Builder.Default
    private boolean hidden = false;

    // ── Domain helpers ==>

    public void incrementHelpfulVotes() {
        this.helpfulVotes++;
    }

    public void hide() {
        this.hidden = true;
    }

    public boolean belongsTo(UUID uid) {
        return this.userId.equals(uid);
    }
}