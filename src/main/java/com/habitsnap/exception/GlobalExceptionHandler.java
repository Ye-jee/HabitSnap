package com.habitsnap.exception;

import com.habitsnap.common.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @RestControllerAdvice 붙여서 전역 예외 처리 담당
/* HabitSnap 전역 예외 처리 핸들러
 - CustomException 및 JWT 예외를 통합 관리
 - 'ApiErrorResponse 형식으로 일관된 JSON 응답 반환'에서
   'ApiResponse.fail()기반으로 응답 형식 통일'로 변경됨
*/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException을 처리하는 핸들러이자 CustomException 잡아서 ApiErrorResponse로 응답 보내는 부분
    /* HabitSnap 전역에서 CustomException 발생 시 ErrorCode 기반으로 통일된 JSON 응답 반환
    * */
    /*@ExceptionHandler(CustomException.class)
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
    }*/

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException customException){
        return ResponseEntity
                .status(customException.getErrorCode().getStatus())
                .body(ApiResponse.fail(customException.getErrorCode()));

    }


    // 검증(Validation) 실패 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException exception){

        // 필드별 에러 메시지 모음 (원하면 테스트에서 $.data.email 같은 식으로 검증 가능)
        List<FieldError> errors = exception.getBindingResult().getFieldErrors();

        Map<String, String> fieldErrors = errors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, fieldErrors));

    }







}
