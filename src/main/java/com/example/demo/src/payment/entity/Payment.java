package com.example.demo.src.payment.entity;

import com.example.demo.common.entity.BaseEntity;
import com.example.demo.src.subscription.entity.Subscription;
import com.example.demo.src.user.entity.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Getter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "merchant_uid", length = 255, nullable = false)
    private String merchantUid;

    @Column(name = "imp_uid", length = 255, nullable = false)
    private String impUid;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "fail_reason", length = 255)
    private String failReason;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAt;

    @Builder
    public Payment(Long id,
                   User user,
                   Subscription subscription,
                   String merchantUid,
                   String impUid,
                   Integer amount,
                   PaymentStatus status,
                   String failReason,
                   LocalDateTime paidAt) {
        this.id = id;
        this.user = user;
        this.subscription = subscription;
        this.merchantUid = merchantUid;
        this.impUid = impUid;
        this.amount = amount;
        this.status = status;
        this.failReason = failReason;
        this.paidAt = paidAt;
    }
}
