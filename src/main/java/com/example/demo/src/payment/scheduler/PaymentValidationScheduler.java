package com.example.demo.src.payment.scheduler;

import com.example.demo.src.payment.PaymentGatewayService;
import com.example.demo.src.payment.PaymentRepository;
import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidationScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService gatewayService;

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void validateRecentPayments() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Payment> payments = paymentRepository.findAllByStatusAndPaidAtAfter(PaymentStatus.PAID, since);
        for (Payment p : payments) {
            try {
                gatewayService.validateAndCancelIfMismatch(p);
            } catch (Exception e) {
                log.warn("결제 검증 실패 id={} impUid={}: {}", p.getId(), p.getImpUid(), e.getMessage());
            }
        }
    }
}

