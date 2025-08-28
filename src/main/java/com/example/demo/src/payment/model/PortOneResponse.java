package com.example.demo.src.payment.model;

import lombok.Getter;

@Getter
public class PortOneResponse {
    private String impUid;
    private String merchantUid;
    private Long taskId;
    private String buyer_name;
}
