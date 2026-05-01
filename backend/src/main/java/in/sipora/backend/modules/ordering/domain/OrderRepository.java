package in.sipora.backend.modules.ordering.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Fetch an order with all items eagerly — avoids N+1 on the order detail page. */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByGatewayOrderId(String gatewayOrderId);

    /** User order history — newest first, paginated. */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /** Flat list for OrderingModuleApi — no pagination, used for cross-module calls. */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Checks if a user has a DELIVERED order containing a specific product.
     * Used by review module to enforce verified-purchase gate.
     */
    @Query("""
           SELECT COUNT(o) > 0 FROM Order o
           JOIN o.items i
           WHERE o.userId = :userId
             AND i.productId = :productId
             AND o.status = 'DELIVERED'
           """)
    boolean existsDeliveredOrderForUserAndProduct(
            @Param("userId")    UUID userId,
            @Param("productId") UUID productId);

    /** Admin — all orders paginated, optionally filtered by status. */
    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findAllForAdmin(
            @Param("status") OrderStatus status,
            Pageable pageable);
}