package in.sipora.backend.modules.payment.application;

import in.sipora.backend.modules.payment.api.PaymentModuleApi;
import in.sipora.backend.modules.payment.api.PaymentStatusInfo;
import in.sipora.backend.modules.payment.domain.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentModuleApiImpl implements PaymentModuleApi {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentStatusInfo> getPaymentForOrder(UUID orderId) {
        return paymentRepository.findCapturedByOrderId(orderId)
                .or(() -> paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                        .stream().findFirst())
                .map(paymentMapper::toStatusInfo);
    }
}