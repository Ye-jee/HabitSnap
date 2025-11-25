package com.habitsnap.api;

import com.habitsnap.application.auth.AuthService;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리함")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequest request){
        authService.singUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인 후 JWT 토큰을 발급함")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

}
