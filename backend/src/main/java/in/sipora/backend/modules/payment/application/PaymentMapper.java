package in.sipora.backend.modules.payment.application;

import in.sipora.backend.modules.payment.api.PaymentStatusInfo;
import in.sipora.backend.modules.payment.domain.Payment;
import in.sipora.backend.modules.payment.web.PaymentDTOs.PaymentResponse;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderId(),
                p.getStatus().name(),
                p.getAmount(),
                p.getCurrencyCode(),
                p.getGatewayOrderId(),
                p.getGatewayPaymentId(),
                p.getFailureReason(),
                p.getCapturedAt(),
                p.getFailedAt(),
                p.getCreatedAt()
        );
    }

    public PaymentStatusInfo toStatusInfo(Payment p) {
        return new PaymentStatusInfo(
                p.getId(),
                p.getOrderId(),
                p.getStatus().name(),
                p.getAmount(),
                p.getCurrencyCode(),
                p.getGatewayOrderId(),
                p.getGatewayPaymentId(),
                p.getCapturedAt(),
                p.getFailedAt()
        );
    }
}