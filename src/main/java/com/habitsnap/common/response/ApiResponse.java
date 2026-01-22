package com.habitsnap.common.response;

import com.habitsnap.dto.mealrecord.MealRecordResponse;
import com.habitsnap.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/* HabitSnap 공통 응답 포맷
* - 성공/실패 모두 일관된 구조로 관리
* - ApiErrorResponse의 코드와 timestamp 개념을 포함 [리팩터링]
* */

@Schema(description = "HabitSnap 공통 API 응답 구조")
@Getter
@Builder
public class ApiResponse<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;           // (예: 200, 201, 404)

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;       // 메세지 (성공/실패 공용)

    @Schema(description = "에러 코드 (예외 발생 시에만 표시)", example = "MEAL_NOT_FOUND")
    private String code;                // 에러 코드 (예: MEAL_NOT_FOUND)

    @Schema(description = "응답 생성 시각", example = "2025-12-04T14:21:00.000")
    private LocalDateTime timestamp;    // 응답 생성 시각 (성공/실패 공통)

    @Schema(description = "응답 데이터 (성공 시 포함)")
    private final T data;               // 실제 응답 데이터 (제네릭)

    // 성공 응답1 (데이터 포함)
    // 사용 예: 로그인 결과, 조회 결과
    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message("요청이 성공적으로 처리되었습니다.")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    // 성공 응답2 (메시지만 포함)
    // 사용 예: 수정 완료, 삭제 완료
    public static <T> ApiResponse<T> success(String message){
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 추가 성공 응답3 (메시지랑 데이터 모두 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    // 실패 응답1 (전역 예외에서 사용 가능 > HTTP 상태랑 메시지 포함)
    public static <T> ApiResponse<T> fail(HttpStatus status, String message){
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답2 (에러코드 기반)
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답3 (에러코드랑 메시지 모두 포함)
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return ApiResponse.<T>builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

}
