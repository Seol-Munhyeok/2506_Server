package com.example.demo.common.oauth;

import com.example.demo.src.user.model.KakaoOAuthToken;
import com.example.demo.src.user.model.KakaoUser;
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
public class KakaoOauth implements SocialOauth {

    @Value("${spring.OAuth2.kakao.url}")
    private String KAKAO_SNS_URL;

    @Value("${spring.OAuth2.kakao.client-id}")
    private String KAKAO_SNS_CLIENT_ID;

    @Value("${spring.OAuth2.kakao.callback-login-url}")
    private String KAKAO_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.OAuth2.kakao.client-secret}")
    private String KAKAO_SNS_CLIENT_SECRET;

    @Value("${spring.OAuth2.kakao.scope}")
    private String KAKAO_DATA_ACCESS_SCOPE;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getOauthRedirectURL() {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", KAKAO_DATA_ACCESS_SCOPE);
        params.put("response_type", "code");
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_LOGIN_URL);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));
        return KAKAO_SNS_URL + "?" + parameterString;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        String KAKAO_TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", KAKAO_SNS_CLIENT_ID);
        params.put("client_secret", KAKAO_SNS_CLIENT_SECRET);
        params.put("redirect_uri", KAKAO_SNS_CALLBACK_LOGIN_URL);
        params.put("grant_type", "authorization_code");

        return restTemplate.postForEntity(KAKAO_TOKEN_REQUEST_URL, params, String.class);
    }

    public KakaoOAuthToken getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        return objectMapper.readValue(response.getBody(), KakaoOAuthToken.class);
    }

    public ResponseEntity<String> requestUserInfo(KakaoOAuthToken oAuthToken) {
        String KAKAO_USERINFO_REQUEST_URL = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(headers);
        return restTemplate.exchange(KAKAO_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
    }

    public KakaoUser getUserInfo(ResponseEntity<String> userInfoRes) throws JsonProcessingException {
        return objectMapper.readValue(userInfoRes.getBody(), KakaoUser.class);
    }
}
