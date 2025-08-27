package com.example.demo.common.oauth;

import com.example.demo.common.Constant;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.src.user.UserService;
import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.model.*;
import com.example.demo.src.user.entity.User;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.demo.common.response.BaseResponseStatus.BLOCKED_USER;
import static com.example.demo.common.response.BaseResponseStatus.DORMANT_USER;
import static com.example.demo.common.response.BaseResponseStatus.INVALID_OAUTH_TYPE;
import static com.example.demo.common.response.BaseResponseStatus.SUSPENDED_USER;
import static com.example.demo.common.response.BaseResponseStatus.WITHDRAWN_USER;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final GoogleOauth googleOauth;
    private final KakaoOauth kakaoOauth;
    private final NaverOauth naverOauth;
    private final AppleOauth appleOauth;
    private final HttpServletResponse response;
    private final UserService userService;
    private final JwtService jwtService;

    public void accessRequest(Constant.SocialLoginType socialLoginType) throws IOException {
        String redirectURL;
        switch (socialLoginType) { //각 소셜 로그인을 요청하면 소셜로그인 페이지로 리다이렉트 해주는 프로세스이다.
            case GOOGLE:
                redirectURL = googleOauth.getOauthRedirectURL();
                break;
            case KAKAO:
                redirectURL = kakaoOauth.getOauthRedirectURL();
                break;
            case NAVER:
                redirectURL = naverOauth.getOauthRedirectURL();
                break;
            case APPLE:
                redirectURL = appleOauth.getOauthRedirectURL();
                break;
            default:
                throw new BaseException(INVALID_OAUTH_TYPE);
        }
        response.sendRedirect(redirectURL);
    }

    public GetSocialOAuthRes oAuthLoginOrJoin(Constant.SocialLoginType socialLoginType, String code) throws IOException {
        switch (socialLoginType) {
            case GOOGLE: {
                ResponseEntity<String> accessTokenResponse = googleOauth.requestAccessToken(code);
                GoogleOAuthToken oAuthToken = googleOauth.getAccessToken(accessTokenResponse);
                ResponseEntity<String> userInfoResponse = googleOauth.requestUserInfo(oAuthToken);
                GoogleUser googleUser = googleOauth.getUserInfo(userInfoResponse);
                return loginOrJoin(googleUser.getEmail(), googleUser.toEntity(), oAuthToken.getAccess_token(), oAuthToken.getToken_type());
            }
            case KAKAO: {
                ResponseEntity<String> accessTokenResponse = kakaoOauth.requestAccessToken(code);
                KakaoOAuthToken oAuthToken = kakaoOauth.getAccessToken(accessTokenResponse);
                ResponseEntity<String> userInfoResponse = kakaoOauth.requestUserInfo(oAuthToken);
                KakaoUser kakaoUser = kakaoOauth.getUserInfo(userInfoResponse);
                return loginOrJoin(kakaoUser.getEmail(), kakaoUser.toEntity(), oAuthToken.getAccess_token(), oAuthToken.getToken_type());
            }
            case NAVER: {
                ResponseEntity<String> accessTokenResponse = naverOauth.requestAccessToken(code);
                NaverOAuthToken oAuthToken = naverOauth.getAccessToken(accessTokenResponse);
                ResponseEntity<String> userInfoResponse = naverOauth.requestUserInfo(oAuthToken);
                NaverUser naverUser = naverOauth.getUserInfo(userInfoResponse);
                return loginOrJoin(naverUser.getEmail(), naverUser.toEntity(), oAuthToken.getAccess_token(), oAuthToken.getToken_type());
            }
            case APPLE: {
                ResponseEntity<String> accessTokenResponse = appleOauth.requestAccessToken(code);
                AppleOAuthToken oAuthToken = appleOauth.getAccessToken(accessTokenResponse);
                ResponseEntity<String> userInfoResponse = appleOauth.requestUserInfo(oAuthToken);
                AppleUser appleUser = appleOauth.getUserInfo(userInfoResponse);
                return loginOrJoin(appleUser.getEmail(), appleUser.toEntity(), oAuthToken.getAccess_token(), oAuthToken.getToken_type());
            }
            default:
                throw new BaseException(INVALID_OAUTH_TYPE);
        }
    }

    private GetSocialOAuthRes loginOrJoin(String email, User userEntity, String accessToken, String tokenType) {
        if (userService.checkUserByEmail(email)) {
            GetUserRes getUserRes = userService.getUserByEmail(email);
            validateAccountStatus(getUserRes);
            String jwtToken = jwtService.createJwt(getUserRes.getId());
            return new GetSocialOAuthRes(jwtToken, getUserRes.getId(), accessToken, tokenType);
        } else {
            PostUserRes postUserRes = userService.createOAuthUser(userEntity);
            return new GetSocialOAuthRes(postUserRes.getJwt(), postUserRes.getId(), accessToken, tokenType);
        }
    }

    private void validateAccountStatus(GetUserRes userRes) {
        if (userRes.getAccountStatus() == null) return;
        AccountStatus status = AccountStatus.valueOf(userRes.getAccountStatus());
        switch (status) {
            case DORMANT:
                throw new BaseException(DORMANT_USER);
            case BLOCKED:
                throw new BaseException(BLOCKED_USER);
            case SUSPENDED:
                throw new BaseException(SUSPENDED_USER);
            case WITHDRAWN:
                throw new BaseException(WITHDRAWN_USER);
            default:
                break;
        }
    }
}