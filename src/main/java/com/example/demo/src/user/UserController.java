package com.example.demo.src.user;


import com.example.demo.common.Constant.SocialLoginType;
import com.example.demo.common.oauth.OAuthService;
import com.example.demo.src.log.entity.Log;
import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.LoginType;
import com.example.demo.utils.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.time.LocalDateTime;
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
    @Operation(summary = "회원가입", description = "신규 회원을 생성합니다")
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-ACCESS-TOKEN", res.getJwt())
                .body(new BaseResponse<>(res));
    }

    /**
     * 회원 조회 API
     * [GET] /users
     * 다양한 조건으로 회원을 검색합니다.
     * @return BaseResponse<List<GetUserRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    @Operation(summary = "회원 목록 조회", description = "전체 회원 또는 이메일로 회원을 조회합니다")
    public BaseResponse<List<GetUserRes>> getUsers(
            @Parameter(description = "페이지 인덱스") @RequestParam(required = false) Integer pageIndex,
            @Parameter(description = "페이지 크기") @RequestParam(required = false) Integer size,
            @Parameter(description = "검색할 유저 ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "검색할 이름") @RequestParam(required = false) String name,
            @Parameter(description = "가입 시작일(yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) String joinedStart,
            @Parameter(description = "가입 종료일(yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) String joinedEnd,
            @Parameter(description = "계정 상태") @RequestParam(required = false) String accountStatus) {
        if (pageIndex == null || size == null) throw new BaseException(PAGINATION_PARAM_MISSING);
        if (pageIndex < 0 || size <= 0) throw new BaseException(PAGINATION_PARAM_INVALID);

        String trimmedName = trimOrNull(name);
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (joinedStart != null) {
            try {
                start = LocalDateTime.parse(joinedStart);
            } catch (Exception e) {
                throw new BaseException(RESPONSE_ERROR);
            }
        }
        if (joinedEnd != null) {
            try {
                end = LocalDateTime.parse(joinedEnd);
            } catch (Exception e) {
                throw new BaseException(RESPONSE_ERROR);
            }
        }

        AccountStatus status = null;
        if (accountStatus != null) {
            try {
                status = AccountStatus.valueOf(accountStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(RESPONSE_ERROR);
            }
        }

        List<GetUserRes> getUsersRes = userService.getUsers(userId, trimmedName, start, end, status, pageIndex, size);
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
    @Operation(summary = "회원 단건 조회", description = "userId로 회원 정보를 조회합니다")
    public BaseResponse<GetUserRes> getUser(@PathVariable("userId") Long userId) {
        if (userId == null || userId <= 0) throw new BaseException(NOT_FIND_USER);
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
    @Operation(summary = "회원 이름 수정", description = "userId로 회원 이름을 수정합니다")
    public BaseResponse<String> modifyUserName(@PathVariable("userId") Long userId, @RequestBody PatchUserReq patchUserReq){

        Long jwtUserId = jwtService.getUserId();
        if (!jwtUserId.equals(userId)) throw new BaseException(INVALID_USER_JWT);

        String name = trimOrNull(patchUserReq.getName());
        if (name == null || name.isEmpty() || name.length() > 20) throw new BaseException(POST_USERS_INVALID_NAME);
        patchUserReq.setName(name);

        userService.modifyUserName(userId, patchUserReq);

        String result = "수정 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 회원 정지 API
     * [PATCH] /app/users/:userId/suspend
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userId}/suspend")
    @Operation(summary = "회원 정지", description = "관리자가 회원을 정지합니다")
    public BaseResponse<String> suspendUser(@PathVariable("userId") Long userId) {
        Long adminId = jwtService.getUserId();
        if (!userService.isAdmin(adminId)) throw new BaseException(INVALID_USER_JWT);

        userService.suspendUser(userId);

        String result = "정지 완료!!";
        return new BaseResponse<>(result);
    }

    /**
     * 로그인 API
     * [POST] /app/users/logIn
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/logIn")
    @Operation(summary = "로그인", description = "로그인 ID와 비밀번호로 로그인합니다")
    public ResponseEntity<BaseResponse<PostLoginRes>> logIn(@RequestBody PostLoginReq postLoginReq){
        String loginId = trimOrNull(postLoginReq.getLoginId());
        String password = trimOrNull(postLoginReq.getPassword());

        if (loginId == null || loginId.isEmpty()) throw new BaseException(POST_USERS_INVALID_LOGIN_ID);
        if (!isRegexId(loginId)) throw new BaseException(POST_USERS_INVALID_LOGIN_ID);
        if (password == null || password.isEmpty()) throw new BaseException(POST_USERS_INVALID_PASSWORD);
        if (password.length() < 6 || password.length() > 20) throw new BaseException(POST_USERS_INVALID_PASSWORD);

        postLoginReq.setLoginId(loginId);
        postLoginReq.setPassword(password);

        PostLoginRes postLoginRes = userService.logIn(postLoginReq);
        return ResponseEntity.ok()
                .header("X-ACCESS-TOKEN", postLoginRes.getJwt())
                .body(new BaseResponse<>(postLoginRes));
    }


    /**
     * 유저 소셜 가입, 로그인 인증으로 리다이렉트 해주는 url
     * [GET] /app/users/auth/:socialLoginType/login
     * @return void
     */
    @GetMapping("/auth/{socialLoginType}/login")
    @Operation(summary = "소셜 로그인 리다이렉트", description = "소셜 로그인 페이지로 리다이렉트합니다")
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
    @Operation(summary = "소셜 로그인 콜백", description = "소셜 로그인 후 콜백을 처리합니다")
    public BaseResponse<GetSocialOAuthRes> socialLoginCallback(
            @PathVariable(name = "socialLoginType") String socialLoginPath,
            @RequestParam(name = "code") String code) throws IOException, BaseException{
        log.info(">> 소셜 로그인 API 서버로부터 받은 code : {}", code);
        SocialLoginType socialLoginType = SocialLoginType.valueOf(socialLoginPath.toUpperCase());
        GetSocialOAuthRes getSocialOAuthRes = oAuthService.oAuthLoginOrJoin(socialLoginType,code);
        return new BaseResponse<>(getSocialOAuthRes);
    }
}
