package com.habitsnap.exception;

// 비즈니스 로직에서 던지는 사용자 정의 예외
// 전역 예외 시스템의 중심축
// 서비스나 도메인에서 “이건 의도된 예외야”라고 던질 때 이 클래스를 사용
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
