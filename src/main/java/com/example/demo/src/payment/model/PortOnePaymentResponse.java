package com.example.demo.src.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PortOnePaymentResponse {
    private int code;
    private String message;
    private PaymentData response;

    @Getter
    public static class PaymentData {
        @JsonProperty("imp_uid")
        private String impUid;
        @JsonProperty("merchant_uid")
        private String merchantUid;
        @JsonProperty("pay_method")
        private String payMethod;
        @JsonProperty("pg_provider")
        private String pgProvider;
        @JsonProperty("pg_tid")
        private String pgTid;
        @JsonProperty("pg_id")
        private String pgId;
        private double amount;
        private String currency;
        @JsonProperty("apply_num")
        private String applyNum;
        @JsonProperty("buyer_name")
        private String buyerName;
        @JsonProperty("card_code")
        private String cardCode;
        @JsonProperty("card_name")
        private String cardName;
        @JsonProperty("card_quota")
        private String cardQuota;
        @JsonProperty("card_number")
        private String cardNumber;
        private String status;
        @JsonProperty("card_type")
        private String cardType;
        @JsonProperty("started_at")
        private Long startedAt;
        @JsonProperty("paid_at")
        private Long paidAt;
        @JsonProperty("canceled_at")
        private Long canceledAt;
        @JsonProperty("failed_at")
        private Long failedAt;
        @JsonProperty("fail_reason")
        private String failReason;
        @JsonProperty("fail_code")
        private String failCode;
    }
}
