package com.example.demo.src.payment;

import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.src.subscription.entity.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByStatusAndPaidAtAfter(PaymentStatus status, LocalDateTime after);

    @Query("select p from Payment p where p.status = 'PAID' and p.paidAt >= :after")
    List<Payment> findRecentPaid(LocalDateTime after);

    java.util.Optional<Payment> findTopBySubscriptionAndStatusOrderByPaidAtDesc(Subscription subscription, PaymentStatus status);
}
