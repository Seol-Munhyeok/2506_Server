package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.src.subscription.entity.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllByStatusAndPaidAtAfter(PaymentStatus status, LocalDateTime after);

    Optional<Payment> findTopBySubscriptionAndStatusOrderByPaidAtDesc(Subscription subscription, PaymentStatus status);

    Optional<Payment> findByMerchantUid(String merchantUid);
}
