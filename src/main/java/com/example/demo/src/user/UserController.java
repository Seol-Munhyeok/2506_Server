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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public String trimOrNull(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 회원가입 API
     * [POST] /app/users
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("")
    public ResponseEntity<BaseResponse<PostUserRes>> createUser(@RequestBody PostUserReq req) {
        String email   = trimOrNull(req.getEmail());
        String loginId = trimOrNull(req.getLoginId());
        String phone   = trimOrNull(req.getPhoneNumber());
        String name    = trimOrNull(req.getName());
        String typeRaw = trimOrNull(req.getLoginType());

        // 1) 이메일
        if (email == null || email.isEmpty()) throw new BaseException(USERS_EMPTY_EMAIL);
        if (!isRegexEmail(email)) throw new BaseException(POST_USERS_INVALID_EMAIL);
        if (userService.isEmailDuplicate(email)) throw new BaseException(POST_USERS_EXISTS_EMAIL);

        // 2) 로그인 ID
        if (loginId == null || loginId.isEmpty()) throw new BaseException(USERS_EMPTY_LOGIN_ID);
        if (!isRegexId(loginId)) throw new BaseException(POST_USERS_INVALID_LOGIN_ID);
        if (userService.isLoginIdDuplicate(loginId)) throw new BaseException(POST_USERS_EXISTS_LOGIN_ID);

        // 3) 로그인 타입
        if (typeRaw == null || typeRaw.isEmpty()) throw new BaseException(USERS_EMPTY_LOGIN_TYPE);
        LoginType loginType;
        try {
            loginType = LoginType.valueOf(typeRaw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BaseException(POST_USERS_INVALID_LOGIN_TYPE);
        }

        // 4) 비밀번호 (LOCAL만)
        if (loginType == LoginType.LOCAL) {
            String pw = req.getPassword();
            if (pw == null) throw new BaseException(USERS_EMPTY_PASSWORD);
            if (pw.length() < 6 || pw.length() > 20) throw new BaseException(POST_USERS_INVALID_PASSWORD);
        } else {
            req.setPassword("NONE"); // 소셜은 서버에서 무시
        }

        // 5) 전화번호
        if (phone == null || phone.isEmpty()) throw new BaseException(POST_USERS_INVALID_PHONE_NUMBER);

        // 6) 이름
        if (name == null || name.isEmpty()) throw new BaseException(POST_USERS_INVALID_NAME);
        if (name.length() > 20) throw new BaseException(POST_USERS_INVALID_NAME);

        // 7) 생년월일
        if (req.getBirthDate() == null) throw new BaseException(POST_USERS_INVALID_BIRTHDATE);
        LocalDate birth;
        try {
            birth = LocalDate.parse(req.getBirthDate());
        } catch (Exception e) {
            throw new BaseException(POST_USERS_INVALID_BIRTHDATE);
        }
        int y = birth.getYear();
        if (y < 1919 || y > 2021) throw new BaseException(POST_USERS_INVALID_BIRTHDATE);
        if (y >= 2016) throw new BaseException(POST_USERS_RESTRICTED_AGE);

        // 8) 약관 동의
        if (!req.isTermsOfServiceAgreed() || !req.isPrivacyConsentStatus() || !req.isLocationServiceAgreed()) {
            throw new BaseException(POST_USERS_NOT_AGREED_TERMS);
        }

        // 9) 저장
        PostUserRes res = userService.createUser(req);

        // 회원가입 성공
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(res));
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
