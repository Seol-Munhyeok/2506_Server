package com.example.demo.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 200 : 요청 성공
     */
    SUCCESS(true, HttpStatus.OK, "SUCCESS", "요청에 성공하였습니다."),


    /**
     * 400 : Request, Response 오류
     */
    USERS_EMPTY_EMAIL(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_EMAIL_EMPTY", "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_EMAIL_REGEX", "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_EMAIL_DUPLICATED", "중복된 이메일입니다."),

    USERS_EMPTY_LOGIN_ID(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_LOGIN_ID_EMPTY", "아이디를 입력해주세요."),
    POST_USERS_INVALID_LOGIN_ID(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_LOGIN_ID_REGEX", "아이디 형식을 확인해주세요."),
    POST_USERS_EXISTS_LOGIN_ID(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_LOGIN_ID_DUPLICATED", "중복된 아이디입니다."),

    USERS_EMPTY_PHONE_NUMBER(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PHONE_NUMBER_EMPTY", "휴대폰 번호를 입력해주세요."),
    POST_USERS_INVALID_PHONE_NUMBER(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PHONE_NUMBER_REGEX", "휴대폰 번호 형식을 확인해주세요."),

    USERS_EMPTY_PASSWORD(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PASSWORD_EMPTY", "비밀번호를 입력해주세요."),
    POST_USERS_INVALID_PASSWORD(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PASSWORD_REGEX", "비밀번호는 6자 이상 20자 이하여야 합니다."),

    POST_USERS_INVALID_NAME(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_NAME_REGEX", "이름은 20자 이하여야 합니다."),

    POST_USERS_INVALID_BIRTHDATE(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_BIRTHDATE_REGEX", "생년월일을 확인해주세요."),
    POST_USERS_RESTRICTED_AGE(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_AGE_RESTRICTED", "만 8세 이상만 가입 가능합니다."),
    POST_USERS_NOT_AGREED_TERMS(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_TERMS_NOT_AGREED", "필수 약관에 모두 동의해야 합니다."),

    USERS_EMPTY_LOGIN_TYPE(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_LOGIN_TYPE_EMPTY", "로그인 타입을 입력해주세요."),
    POST_USERS_INVALID_LOGIN_TYPE(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_LOGIN_TYPE_INVALID", "지원하지 않는 로그인 타입입니다."),

    PAGINATION_PARAM_MISSING(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PAGINATION_MISSING", "페이지네이션 정보가 누락되었습니다."),
    PAGINATION_PARAM_INVALID(false, HttpStatus.BAD_REQUEST, "ERROR_REQ_PARAM_PAGINATION_INVALID", "페이지네이션 정보가 올바르지 않습니다."),

    EMPTY_JWT(false, HttpStatus.UNAUTHORIZED, "ERROR_AUTH_JWT_EMPTY", "JWT를 입력해주세요."),
    INVALID_JWT(false, HttpStatus.UNAUTHORIZED, "ERROR_AUTH_JWT_INVALID", "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false, HttpStatus.FORBIDDEN, "ERROR_AUTH_JWT_FORBIDDEN", "권한이 없는 유저의 접근입니다."),

    INVALID_OAUTH_TYPE(false, HttpStatus.BAD_REQUEST, "ERROR_AUTH_OAUTH_INVALID", "알 수 없는 소셜 로그인 형식입니다."),

    RESPONSE_ERROR(false, HttpStatus.NOT_FOUND, "ERROR_RESOURCE_NOT_FOUND", "값을 불러오는데 실패하였습니다."),
    NOT_FIND_USER(false, HttpStatus.NOT_FOUND, "ERROR_RESOURCE_USER_NOT_FOUND", "일치하는 유저가 없습니다."),

    FAILED_TO_LOGIN(false, HttpStatus.UNAUTHORIZED, "ERROR_LOGIN-FAILED", "아이디 또는 비밀번호가 일치하지 않습니다."),
    DORMANT_USER(false, HttpStatus.FORBIDDEN, "ERROR_USER_STATUS_DORMANT", "휴면 상태의 유저입니다."),
    BLOCKED_USER(false, HttpStatus.FORBIDDEN, "ERROR_USER_STATUS_BLOCKED", "차단된 유저입니다."),
    SUSPENDED_USER(false, HttpStatus.FORBIDDEN, "ERROR_USER_STATUS_SUSPENDED", "정지된 유저입니다."),
    WITHDRAWN_USER(false, HttpStatus.FORBIDDEN, "ERROR_USER_STATUS_WITHDRAWN", "탈퇴한 유저입니다."),

    SUBSCRIPTION_REQUIRED(false, HttpStatus.FORBIDDEN, "ERROR_USER_NOT_SUBSCRIBED", "구독하지 않은 유저입니다."),

    /**
     * 500 :  Database, Server 오류
     */
    DATABASE_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_SERVER_DATABASE", "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_SERVER_CONNECTION", "서버와의 연결에 실패하였습니다."),
    PASSWORD_ENCRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_SERVER_PW_ENCRYPT", "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_SERVER_PW_DECRYPT", "비밀번호 복호화에 실패하였습니다."),
    UNEXPECTED_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_SERVER_UNEXPECTED", "예상치 못한 에러가 발생했습니다.");


    private final boolean success;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    BaseResponseStatus(boolean success, HttpStatus httpStatus, String code, String message) {
        this.success = success;
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
