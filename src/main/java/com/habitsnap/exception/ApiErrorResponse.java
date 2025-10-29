package com.habitsnap.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 에러 응답의 데이터 포맷 (status, code, message 등)
@Getter
@Builder
public class ApiErrorResponse {

    private final int status;               // HTTP 상태 코드
    private final String code;              // ErrorCode의 이름 (예 : RESOURCE_NOT_FOUND)
    private final String message;           // 사용자에게 보여줄 메시지
    private final LocalDateTime timestamp;  // 에러 발생 시각

    // 빌더로만 생성되게 하고, 타임스탬프는 기본값 자동 설정
    public static ApiErrorResponseBuilder builder(){
        return new ApiErrorResponseBuilder()
                .timestamp(LocalDateTime.now());
    }

}
