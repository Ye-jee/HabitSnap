package com.habitsnap.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;

// @RestControllerAdvice 붙여서 전역 예외 처리 담당
/* HabitSnap 전역 예외 처리 핸들러
 - CustomException 및 JWT 예외를 통합 관리
 - ApiErrorResponse 형식으로 일관된 JSON 응답 반환
*/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException을 처리하는 핸들러이자 CustomException 잡아서 ApiErrorResponse로 응답 보내는 부분
    /* HabitSnap 전역에서 CustomException 발생 시 ErrorCode 기반으로 통일된 JSON 응답 반환
    * */
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
