package in.sipora.backend.modules.payment.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight payment projection shared across module boundaries.
 * Currently no other module calls PaymentModuleApi, but the interface
 * is here for completeness and future use (e.g. admin dashboard module).
 */
public record PaymentStatusInfo(
        UUID paymentId,
        UUID orderId,
        String status,           // PENDING, CAPTURED, FAILED, REFUNDED
        BigDecimal amount,
        String currencyCode,
        String gatewayOrderId,
        String gatewayPaymentId,
        Instant capturedAt,
        Instant failedAt
) {}