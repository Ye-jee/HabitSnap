package com.habitsnap.api;

import com.habitsnap.application.auth.AuthService;
import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.docs.auth.AuthApiDocs;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @AuthApiDocs.SignUpDocs
    @Operation(summary = "회원가입 API", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입을 처리함. (단 비밀번호는 8자리 이상이어야 함)")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequest request){
        authService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입에 성공했습니다."));
    }

    @AuthApiDocs.LoginDocs
    @Operation(summary = "로그인(인증 및 JWT 발급) API ", description = "이메일과 비밀번호로 로그인 후 JWT 토큰을 발급함.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);

        Map<String, String> data = Map.of("accessToken", token);

        return ResponseEntity.ok(
                ApiResponse.success("로그인에 성공했습니다", data));
    }

}
