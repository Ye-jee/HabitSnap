package com.habitsnap.api;

import com.habitsnap.application.HelloService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 샘플 컨트롤러
@Tag(name = "Hello", description = "Swagger 연결 테스트용 API")

@Hidden
@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @PostMapping
    public String createHello(@RequestParam String message){
        return helloService.createHello(message);
    }

    @GetMapping
    public String hello(){
        return "🚀 HabitSnap Server is Running!";
    }

    @Operation(summary = "헬로 엔드포인트", description = "Swagger 설정이 정상 작동하는지 확인합니다.")
    @GetMapping("/swagger")
    public String hello_swagger() {
        return "Hello, HabitSnap!";
    }

}
