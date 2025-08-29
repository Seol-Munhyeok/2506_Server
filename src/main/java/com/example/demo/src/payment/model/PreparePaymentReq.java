package com.example.demo.src.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PreparePaymentReq {
    @JsonProperty("merchant_uid")
    private String merchantUid;
}

