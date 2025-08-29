package com.example.demo.src.subscription.entity;

import com.example.demo.common.entity.BaseEntity;
import com.example.demo.src.payment.entity.Payment;
import com.example.demo.src.user.entity.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    private List<SubscriptionHistory> histories = new ArrayList<>();

    @Builder
    public Subscription(Long id, User user) {
        this.id = id;
        this.user = user;
    }

    public SubscriptionHistory activate(LocalDate startDate, LocalDate endDate, LocalDateTime paymentDate) {
        this.user.activateSubscription();
        return SubscriptionHistory.builder()
                .subscription(this)
                .user(this.user)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .paymentDate(paymentDate)
                .build();
    }

    public SubscriptionHistory cancelPending() {
        this.user.cancelSubscription();
        this.state = State.INACTIVE;
        return SubscriptionHistory.builder()
                .subscription(this)
                .user(this.user)
                .status(SubscriptionStatus.CANCELED)
                .build();
    }

    public SubscriptionHistory cancel(LocalDate startDate, LocalDate endDate, LocalDateTime paymentDate) {
        this.user.cancelSubscription();
        this.state = State.INACTIVE;
        return SubscriptionHistory.builder()
                .subscription(this)
                .user(this.user)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.CANCELED)
                .paymentDate(paymentDate)
                .build();
    }

    public SubscriptionHistory expire(LocalDate startDate, LocalDate endDate, LocalDateTime paymentDate) {
        this.user.cancelSubscription();
        return SubscriptionHistory.builder()
                .subscription(this)
                .user(this.user)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.EXPIRED)
                .paymentDate(paymentDate)
                .build();
    }
}