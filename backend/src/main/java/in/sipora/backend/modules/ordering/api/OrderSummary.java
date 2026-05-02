package in.sipora.backend.modules.ordering.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight order projection shared across module boundaries.
 *
 * Used by:
 *  - payment module → reads orderId, totalAmount, gatewayOrderId to manage payments
 *  - review module → checks productIds to verify purchase before allowing review
 *
 * Plain Java record — no JPA, no Jackson annotations.
 */
public record OrderSummary(
        UUID orderId,
        String orderNumber,
        UUID userId,
        String status,
        BigDecimal totalAmount,
        BigDecimal subtotal,
        BigDecimal shippingCharge,
        String currencyCode,
        String gatewayOrderId,  // Razorpay order ID — null until PaymentService sets it
        List<UUID> productIds,      // for review module's hasUserPurchasedProduct check
        List<UUID> variantIds,      // for stock restoration on cancellation
        Instant createdAt
) {}