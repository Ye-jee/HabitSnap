package com.habitsnap.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LoginRequest", description = "로그인 요청 DTO")
@Getter
@Setter
public class LoginRequest {     // 로그인할 때 입력받는 데이터(이메일, 비밀번호)

    @Email
    @NotBlank
    @Schema(examples = "test@habitsnap.com")
    private String email;

    @NotBlank
    @Schema(examples = "password1234!")
    private String password;


}
