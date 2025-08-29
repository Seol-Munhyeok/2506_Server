package com.example.demo.src.payment.model;

import java.time.LocalDateTime;

import com.example.demo.src.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetPaymentRes {
    private Long id;
    private String merchantUid;
    private String impUid;
    private Integer amount;
    private PaymentStatus status;
    private String failReason;
    private LocalDateTime paidAt;
}
