package com.habitsnap.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SignUpRequest", description = "회원가입 요청 DTO")
@Getter
@Setter
public class SignUpRequest {    // 회원가입 DTO

    @Email
    @NotBlank
    @Schema(examples = "test@habitsnap.com")
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Schema(examples = "password1234!")
    private String password;

    @NotBlank
    @Schema(examples = "해빗스냅유저")
    private String nickname;
}
