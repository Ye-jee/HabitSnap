package com.habitsnap.exception;

import org.springframework.http.HttpStatus;

// 에러 코드/메시지/HTTP 상태를 정의하는 열거형
public enum ErrorCode {

    // 예시 코드
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 추가 코드
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 등록된 이메일입니다.");

    private final HttpStatus status;    // 실제 상태 코드 (400, 404, 500 등)
    private final String message;       // 사용자에게 전달할 에러 메시지

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
