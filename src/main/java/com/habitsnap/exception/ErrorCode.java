package com.habitsnap.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// 에러 코드, HTTP 상태, 메시지를 정의하는 열거형
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 기본 예외
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 회원가입 관련 예외 코드
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),

    // 로그인 관련 예외 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // 인가(권한) 실패 - 403 <추가>
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // JWT 관련 에러 코드
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 존재하지 않습니다."),
    INVALID_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "Authorization 헤더 형식이 잘못되었습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않은 서명입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰 구조입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 식사기록 관련 에러 코드
    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "식사 기록을 찾을 수 없습니다.");


    private final HttpStatus status;    // 실제 상태 코드 (400, 404, 500 등)
    private final String message;       // 사용자에게 전달할 에러 메시지

}
