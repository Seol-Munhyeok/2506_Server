package com.example.demo.src.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PortOneTokenResponse {
    private int code;
    private String message;
    private TokenData response;

    @Getter
    public static class TokenData {
        @JsonProperty("access_token")
        private String accessToken;
    }
}
