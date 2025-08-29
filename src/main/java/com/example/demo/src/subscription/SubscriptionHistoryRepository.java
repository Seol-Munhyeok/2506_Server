package com.example.demo.src.subscription;

import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.subscription.entity.SubscriptionHistory;
import com.example.demo.src.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    Optional<SubscriptionHistory> findTopBySubscriptionOrderByCreatedAtDesc(Subscription subscription);
    Optional<SubscriptionHistory> findTopBySubscriptionAndStatusOrderByCreatedAtDesc(Subscription subscription, SubscriptionStatus status);
    List<SubscriptionHistory> findAllByStatusAndEndDateBetween(SubscriptionStatus status, LocalDate start, LocalDate end);
}