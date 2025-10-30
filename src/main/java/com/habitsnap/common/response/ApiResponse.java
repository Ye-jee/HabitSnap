package com.habitsnap.common.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ApiResponse<T> {

    private final int status;       // HTTP 상태 코드 (예: 200, 201)
    private final String message;   // 성공 메시지
    private final T data;           // 실제 응답 데이터 (제네릭)

    // 성공 응답 (데이터 포함)
    // 사용 예: 로그인 결과, 조회 결과
    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    // 성공 응답 (메시지만 포함)
    // 사용 예: 수정 완료, 삭제 완료
    public static <T> ApiResponse<T> success(String message){
        return ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    // 실패 응답 (전역 예외에서 사용 가능)
    public static <T> ApiResponse<T> fail(HttpStatus status, String message){
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .build();
    }



}
