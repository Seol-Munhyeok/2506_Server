package com.example.demo.src.subscription;

import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import static com.example.demo.common.entity.BaseEntity.State;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUserOrderByCreatedAtDesc(User user);
    Optional<Subscription> findTopByUserAndStateOrderByCreatedAtDesc(User user, State state);
}
