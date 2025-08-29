package com.example.demo.src.subscription.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SubscriptionStatusRes {
    private boolean active;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}

