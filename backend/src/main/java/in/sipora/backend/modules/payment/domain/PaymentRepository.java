package in.sipora.backend.modules.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /** Find by our internal orderId — may return multiple (failed then captured). */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(UUID orderId);

    /** Find the most recent CAPTURED payment for an order. */
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.status = 'CAPTURED' " +
            "ORDER BY p.capturedAt DESC")
    Optional<Payment> findCapturedByOrderId(@Param("orderId") UUID orderId);

    /** Look up by Razorpay's order ID — used in webhook handler. */
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    /** Look up by Razorpay payment ID — used for idempotency in webhook. */
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    /**
     * Check if a PENDING payment already exists for this gateway order.
     * Used to prevent duplicate createOrder calls.
     */
    boolean existsByGatewayOrderIdAndStatus(String gatewayOrderId, PaymentStatus status);
}