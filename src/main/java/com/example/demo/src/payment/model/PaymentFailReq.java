package com.example.demo.src.payment.model;

import lombok.Getter;

@Getter
public class PaymentFailReq {
    private String merchantUid;
    private String reason;
}

