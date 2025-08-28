package com.example.demo.src.payment.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyPaymentRes {
    private String status;      // PAID / CANCELLED
    private Integer amount;
    private String impUid;
    private String merchantUid;
    private String failReason;
}

