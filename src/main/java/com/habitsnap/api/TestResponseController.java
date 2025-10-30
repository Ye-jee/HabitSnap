package com.habitsnap.api;

import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestResponseController {

    // 단순 성공 메시지 테스트
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> testSuccessMessage(){    // getSuccess()
        return ResponseEntity.ok(ApiResponse.success("요청이 성공적으로 처리되었습니다."));
    }

    // 데이터 포함 성공 테스트
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testSuccessWithData(){  // getData()
        Map<String, Object> sampleData = Map.of(
                "userId" , 1,
               "nickname", "수연",
               "goal", "꾸준히 기록하기"
        );
        return ResponseEntity.ok(ApiResponse.success(sampleData));
    }

    // 실패 응답 테스트용 (예외 던지기)
    @GetMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> getFail(){
        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }


}
