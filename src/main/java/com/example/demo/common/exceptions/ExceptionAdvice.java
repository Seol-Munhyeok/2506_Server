package com.example.demo.common.exceptions;

import com.example.demo.common.response.BaseResponse;
import com.example.demo.common.response.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Object>> BaseExceptionHandle(BaseException exception) {
        BaseResponseStatus status = exception.getStatus();
        log.warn("BaseException: code={}, message={}", status.getCode(), status.getMessage());
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(new BaseResponse<>(status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> ExceptionHandle(Exception exception) {
        log.error("Unexpected exception", exception);
        BaseResponseStatus status = BaseResponseStatus.UNEXPECTED_ERROR;
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(new BaseResponse<>(status));
    }
}
