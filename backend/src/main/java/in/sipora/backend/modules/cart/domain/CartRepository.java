package in.sipora.backend.modules.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Finds the active cart for a logged-in user.
     * Eagerly fetches items to avoid N+1 when rendering the cart page.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i " +
            "WHERE c.userId = :userId AND c.status = 'ACTIVE' " +
            "ORDER BY c.createdAt DESC")
    List<Cart> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Finds the active guest cart by frontend-generated sessionId.
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i " +
            "WHERE c.sessionId = :sessionId AND c.status = 'ACTIVE' " +
            "AND c.userId IS NULL")
    Optional<Cart> findActiveGuestCart(@Param("sessionId") String sessionId);

    /**
     * Marks abandoned carts older than the given threshold.
     * Called by a scheduled job (e.g. nightly @Scheduled method).
     */
    @Modifying
    @Query("UPDATE Cart c SET c.status = 'ABANDONED' " +
            "WHERE c.status = 'ACTIVE' AND c.updatedAt < :threshold")
    int markAbandonedBefore(@Param("threshold") Instant threshold);
}