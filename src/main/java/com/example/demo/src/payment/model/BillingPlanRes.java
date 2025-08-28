package com.example.demo.src.payment.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingPlanRes {
    private String planId;
    private int amount;
    private String currency;
    private String interval;
    private String label;
}
