package com.certsniper.global.exception;

import lombok.Getter;

/** 비즈니스 규칙 위반 예외. GlobalExceptionHandler 에서 ErrorResponse 로 변환된다. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 기본 메시지 대신 사용할 메시지 지정 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
