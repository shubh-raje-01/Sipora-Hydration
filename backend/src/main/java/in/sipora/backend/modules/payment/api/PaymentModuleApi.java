package in.sipora.backend.modules.payment.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Public API of the payment module.
 *
 * Currently no other module calls this — payment status is communicated
 * back to the ordering module directly inside PaymentService via
 * OrderingModuleApi.confirmOrder() / cancelOrder().
 *
 * Exposed here for:
 *  - Future admin dashboard module (revenue reporting)
 *  - Potential refund orchestration from an order management module
 */
public interface PaymentModuleApi {

    /** Returns payment status for a given order. Empty if no payment record exists yet. */
    Optional<PaymentStatusInfo> getPaymentForOrder(UUID orderId);
}