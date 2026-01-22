package com.habitsnap.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {     // 로그인할 때 입력받는 데이터(이메일, 비밀번호)

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;


}
