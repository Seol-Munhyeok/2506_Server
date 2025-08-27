package com.example.demo.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.demo.common.response.BaseResponseStatus.SUCCESS;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "code", "message", "result"})
public class BaseResponse<T> {
    @JsonProperty("success")
    private final boolean success;

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    /** 성공 응답 */
    public BaseResponse(T result) {
        this.success = SUCCESS.isSuccess();
        this.code = SUCCESS.getCode();         // "SUCCESS"
        this.message = SUCCESS.getMessage();   // "요청에 성공하였습니다."
        this.result = result;
    }

    /** 실패 응답 */
    public BaseResponse(BaseResponseStatus status) {
        this.success = status.isSuccess();
        this.code = status.getCode();          // 문자열 식별자
        this.message = status.getMessage();
        this.result = null;
    }
}

