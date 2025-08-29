package com.example.demo.src.payment.scheduler;

import com.example.demo.src.payment.PaymentGatewayService;
import com.example.demo.src.subscription.SubscriptionHistoryRepository;
import com.example.demo.src.subscription.entity.SubscriptionHistory;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentScheduler {

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PaymentGatewayService paymentGatewayService;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void chargeExpiringSubscriptions() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        List<SubscriptionHistory> expiring = subscriptionHistoryRepository
                .findAllByStatusAndEndDateBetween(SubscriptionStatus.ACTIVE, today, tomorrow);
        for (SubscriptionHistory history : expiring) {
            try {
                String merchantUid = "sub_" + history.getSubscription().getId() + "_" + UUID.randomUUID();
                paymentGatewayService.requestPayment(merchantUid);
                paymentGatewayService.recordRequested(history.getUser(), history.getSubscription(), merchantUid);
            } catch (Exception e) {
                log.warn("정기 결제 요청 실패 subscriptionId={}: {}", history.getSubscription().getId(), e.getMessage());
            }
        }
    }
}

