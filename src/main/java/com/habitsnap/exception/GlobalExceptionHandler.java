package com.habitsnap.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice 붙여서 전역 예외 처리 담당
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException을 처리하는 핸들러이자 CustomException 잡아서 ApiErrorResponse로 응답 보내는 부분
    /* 해당 코드는 “HabitSnap에서 CustomException이 발생하면 ErrorCode 기준으로 통일된 JSON 응답을 반환” 하는 구조 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException customException){
        ErrorCode errorCode = customException.getErrorCode();

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(apiErrorResponse);
    }


}
