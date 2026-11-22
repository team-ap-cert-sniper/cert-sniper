package com.certsniper.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 공통 오류 응답 (명세서 10장).
 * code, message, timestamp, errors(검증 오류일 때만 포함)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        List<FieldError> errors
) {

    /** 필드별 검증 오류 */
    public record FieldError(String field, String reason) {}

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, LocalDateTime.now(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> errors) {
        return new ErrorResponse(errorCode.name(), message, LocalDateTime.now(), errors);
    }
}
