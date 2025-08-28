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

    @Column(name = "start_date", updatable = false)
    private LocalDate startDate;

    @Column(name = "end_date", updatable = false)
    private LocalDate endDate;

    @Column(length = 20, nullable = false)
    private SubscriptionStatus status;

    @Column(name = "payment_date", columnDefinition = "TIMESTAMP", updatable = false)
    private LocalDateTime paymentDate;

    @Builder
    public Subscription(Long id, User user, LocalDate startDate, LocalDate endDate,
                        SubscriptionStatus status, LocalDateTime paymentDate) {
        this.id = id;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public Subscription activate(LocalDate startDate, LocalDate endDate, LocalDateTime paymentDate) {
        this.user.activateSubscription();
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE;
        this.paymentDate = paymentDate;
        return this;
    }

    public Subscription cancelPending() {
        this.user.cancelSubscription();
        this.status = SubscriptionStatus.CANCELED;
        this.state = State.INACTIVE;
        return this;
    }

    public Subscription cancel(LocalDate endDate) {
        this.user.cancelSubscription();
        this.state = State.INACTIVE;
        return Subscription.builder()
                .user(this.user)
                .startDate(this.startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.CANCELED)
                .paymentDate(this.paymentDate)
                .build();
    }

    public Subscription expire(LocalDate endDate) {
        this.user.cancelSubscription();
        return Subscription.builder()
                .user(this.user)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.EXPIRED)
                .paymentDate(this.paymentDate)
                .build();
    }
}