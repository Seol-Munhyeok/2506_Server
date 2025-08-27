package com.example.demo.common.oauth;

import com.example.demo.src.user.model.AppleOAuthToken;
import com.example.demo.src.user.model.AppleUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOauth implements SocialOauth {

    @Value("${spring.OAuth2.apple.url}")
    private String APPLE_SNS_URL;

    @Value("${spring.OAuth2.apple.client-id}")
    private String APPLE_SNS_CLIENT_ID;

    @Value("${spring.OAuth2.apple.callback-login-url}")
    private String APPLE_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.apple.client-secret}")
    private String APPLE_SNS_CLIENT_SECRET;

    @Value("${spring.OAuth2.apple.scope}")
    private String APPLE_DATA_ACCESS_SCOPE;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", APPLE_DATA_ACCESS_SCOPE);
        params.put("response_type", "code");
        params.put("client_id", APPLE_SNS_CLIENT_ID);
        params.put("redirect_uri", APPLE_SNS_CALLBACK_LOGIN_URL);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));
        return APPLE_SNS_URL + "?" + parameterString;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        String APPLE_TOKEN_REQUEST_URL = "https://appleid.apple.com/auth/token";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", APPLE_SNS_CLIENT_ID);
        params.put("client_secret", APPLE_SNS_CLIENT_SECRET);
        params.put("redirect_uri", APPLE_SNS_CALLBACK_LOGIN_URL);
        params.put("grant_type", "authorization_code");

        return restTemplate.postForEntity(APPLE_TOKEN_REQUEST_URL, params, String.class);
    }

    public AppleOAuthToken getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), AppleOAuthToken.class);
    }

    public ResponseEntity<String> requestUserInfo(AppleOAuthToken oAuthToken) {
        String APPLE_USERINFO_REQUEST_URL = "https://appleid.apple.com/auth/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);
        return restTemplate.exchange(APPLE_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
    }

    public AppleUser getUserInfo(ResponseEntity<String> userInfoRes) throws JsonProcessingException {
        return objectMapper.readValue(userInfoRes.getBody(), AppleUser.class);
    }
}