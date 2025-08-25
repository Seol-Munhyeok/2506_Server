package com.example.demo.src.user;


import com.example.demo.common.Constant.SocialLoginType;
import com.example.demo.common.oauth.OAuthService;
import com.example.demo.src.log.entity.Log;
import com.example.demo.src.user.entity.LoginType;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponse;
import com.example.demo.src.user.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


import static com.example.demo.common.response.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;
import static com.example.demo.utils.ValidationRegex.isRegexId;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/users")
public class UserController {


    private final UserService userService;

    private final OAuthService oAuthService;

    private final JwtService jwtService;


    /**
     * 회원가입 API
     * [POST] /app/users
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        if (postUserReq.getEmail() == null) {
            return new BaseResponse<>(USERS_EMPTY_EMAIL);
        }
        //이메일 정규표현
        if (!isRegexEmail(postUserReq.getEmail())) {
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        if (postUserReq.getLoginId() == null || postUserReq.getLoginId().isEmpty()) {
            return new BaseResponse<>(USERS_EMPTY_LOGIN_ID);
        }
        if (!isRegexId(postUserReq.getLoginId())) {
            return new BaseResponse<>(POST_USERS_INVALID_LOGIN_ID);
        }
        if (userService.isLoginIdDuplicate(postUserReq.getLoginId())) {
            return new BaseResponse<>(POST_USERS_EXISTS_LOGIN_ID);
        }
        LoginType loginType;
        try {
            loginType = LoginType.valueOf(postUserReq.getLoginType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return new BaseResponse<>(POST_USERS_INVALID_LOGIN_TYPE);
        }

        if (loginType == LoginType.LOCAL) {
            if (postUserReq.getPassword() == null || postUserReq.getPassword().length() < 6 || postUserReq.getPassword().length() > 20) {
                return new BaseResponse<>(postUserReq.getPassword() == null ? USERS_EMPTY_PASSWORD : POST_USERS_INVALID_PASSWORD);
            }
        } else {
            postUserReq.setPassword("NONE");  // 소셜 로그인이라면 패스워드를 임의의 값으로 설정
        }

        if (postUserReq.getPhoneNumber() == null || postUserReq.getPhoneNumber().isEmpty()) {
            return new BaseResponse<>(POST_USERS_INVALID_PHONE_NUMBER);
        }

        if (postUserReq.getName() == null || postUserReq.getName().isEmpty()) {
            return new BaseResponse<>(POST_USERS_INVALID_NAME);
        }

        if (postUserReq.getName().length() > 20) {
            return new BaseResponse<>(POST_USERS_INVALID_NAME);
        }

        if (postUserReq.getBirthDate() == null) {
            return new BaseResponse<>(POST_USERS_INVALID_BIRTHDATE);
        }
        LocalDate birth;
        try {
            birth = LocalDate.parse(postUserReq.getBirthDate());
        } catch (java.time.format.DateTimeParseException e) {
            return new BaseResponse<>(POST_USERS_INVALID_BIRTHDATE);
        }

        int birthYear = birth.getYear();
        if (birthYear < 1919 || birthYear > 2021) {
            return new BaseResponse<>(POST_USERS_INVALID_BIRTHDATE);
        }
        if (birthYear >= 2016) {
            return new BaseResponse<>(POST_USERS_RESTRICTED_AGE);
        }

        if (!postUserReq.isTermsOfServiceAgreed() || !postUserReq.isPrivacyConsentStatus() || !postUserReq.isLocationServiceAgreed()) {
            return new BaseResponse<>(POST_USERS_NOT_AGREED_TERMS);
        }


        PostUserRes postUserRes = userService.createUser(postUserReq);
        return new BaseResponse<>(postUserRes);
    }

    /**
     * 회원 조회 API
     * [GET] /users
     * 회원 번호 및 이메일 검색 조회 API
     * [GET] /app/users? Email=
     * @return BaseResponse<List<GetUserRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    public BaseResponse<List<GetUserRes>> getUsers(@RequestParam(required = false) String Email) {
        if(Email == null){
            List<GetUserRes> getUsersRes = userService.getUsers();
            return new BaseResponse<>(getUsersRes);
        }
        // Get Users
        List<GetUserRes> getUsersRes = userService.getUsersByEmail(Email);
        return new BaseResponse<>(getUsersRes);
    }

    /**
     * 회원 1명 조회 API
     * [GET] /app/users/:userId
     * @return BaseResponse<GetUserRes>
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{userId}") // (GET) 127.0.0.1:9000/app/users/:userId
    public BaseResponse<GetUserRes> getUser(@PathVariable("userId") Long userId) {
        GetUserRes getUserRes = userService.getUser(userId);
        return new BaseResponse<>(getUserRes);
    }



    /**
     * 유저정보변경 API
     * [PATCH] /app/users/:userId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}")
    public BaseResponse<String> modifyUserName(@PathVariable("userId") Long userId, @RequestBody PatchUserReq patchUserReq){

        Long jwtUserId = jwtService.getUserId();

        userService.modifyUserName(userId, patchUserReq);

        String result = "수정 완료!!";
        return new BaseResponse<>(result);

    }

    /**
     * 유저정보삭제 API
     * [DELETE] /app/users/:userId
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/{userId}")
    public BaseResponse<String> deleteUser(@PathVariable("userId") Long userId){
        Long jwtUserId = jwtService.getUserId();

        userService.deleteUser(userId);

        String result = "삭제 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 로그인 API
     * [POST] /app/users/logIn
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/logIn")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq){
        // TODO: 로그인 값들에 대한 형식적인 validatin 처리해주셔야합니다!
        // TODO: 유저의 status ex) 비활성화된 유저, 탈퇴한 유저 등을 관리해주고 있다면 해당 부분에 대한 validation 처리도 해주셔야합니다.
        PostLoginRes postLoginRes = userService.logIn(postLoginReq);
        return new BaseResponse<>(postLoginRes);
    }


    /**
     * 유저 소셜 가입, 로그인 인증으로 리다이렉트 해주는 url
     * [GET] /app/users/auth/:socialLoginType/login
     * @return void
     */
    @GetMapping("/auth/{socialLoginType}/login")
    public void socialLoginRedirect(@PathVariable(name="socialLoginType") String SocialLoginPath) throws IOException {
        SocialLoginType socialLoginType= SocialLoginType.valueOf(SocialLoginPath.toUpperCase());
        oAuthService.accessRequest(socialLoginType);
    }


    /**
     * Social Login API Server 요청에 의한 callback 을 처리
     * @param socialLoginPath (GOOGLE, FACEBOOK, NAVER, KAKAO)
     * @param code API Server 로부터 넘어오는 code
     * @return SNS Login 요청 결과로 받은 Json 형태의 java 객체 (access_token, jwt_token, user_num 등)
     */
    @ResponseBody
    @GetMapping(value = "/auth/{socialLoginType}/login/callback")
    public BaseResponse<GetSocialOAuthRes> socialLoginCallback(
            @PathVariable(name = "socialLoginType") String socialLoginPath,
            @RequestParam(name = "code") String code) throws IOException, BaseException{
        log.info(">> 소셜 로그인 API 서버로부터 받은 code : {}", code);
        SocialLoginType socialLoginType = SocialLoginType.valueOf(socialLoginPath.toUpperCase());
        GetSocialOAuthRes getSocialOAuthRes = oAuthService.oAuthLoginOrJoin(socialLoginType,code);
        return new BaseResponse<>(getSocialOAuthRes);
    }


}
