package com.example.demo.src.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PaymentFailReq {
    @JsonProperty("merchant_uid")
    private String merchantUid;
    private String reason;
}

