package com.example.demo.common.oauth;

import com.example.demo.src.user.model.NaverOAuthToken;
import com.example.demo.src.user.model.NaverUser;
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
public class NaverOauth implements SocialOauth {

    @Value("${spring.OAuth2.naver.url}")
    private String NAVER_SNS_URL;

    @Value("${spring.OAuth2.naver.client-id}")
    private String NAVER_SNS_CLIENT_ID;

    @Value("${spring.OAuth2.naver.callback-login-url}")
    private String NAVER_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.naver.client-secret}")
    private String NAVER_SNS_CLIENT_SECRET;

    @Value("${spring.OAuth2.naver.scope}")
    private String NAVER_DATA_ACCESS_SCOPE;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", NAVER_DATA_ACCESS_SCOPE);
        params.put("response_type", "code");
        params.put("client_id", NAVER_SNS_CLIENT_ID);
        params.put("redirect_uri", NAVER_SNS_CALLBACK_LOGIN_URL);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));
        return NAVER_SNS_URL + "?" + parameterString;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        String NAVER_TOKEN_REQUEST_URL = "https://nid.naver.com/oauth2.0/token";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", NAVER_SNS_CLIENT_ID);
        params.put("client_secret", NAVER_SNS_CLIENT_SECRET);
        params.put("redirect_uri", NAVER_SNS_CALLBACK_LOGIN_URL);
        params.put("grant_type", "authorization_code");

        return restTemplate.postForEntity(NAVER_TOKEN_REQUEST_URL, params, String.class);
    }

    public NaverOAuthToken getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), NaverOAuthToken.class);
    }

    public ResponseEntity<String> requestUserInfo(NaverOAuthToken oAuthToken) {
        String NAVER_USERINFO_REQUEST_URL = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);
        return restTemplate.exchange(NAVER_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
    }

    public NaverUser getUserInfo(ResponseEntity<String> userInfoRes) throws JsonProcessingException {
        return objectMapper.readValue(userInfoRes.getBody(), NaverUser.class);
    }
}
