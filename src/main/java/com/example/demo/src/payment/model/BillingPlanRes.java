package com.example.demo.src.payment.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingPlanRes {
    private String planId;
    private int amount;      // KRW
    private String currency; // "KRW"
    private String interval; // "month"
    private String label;    // e.g., "월 9,900원"
}

