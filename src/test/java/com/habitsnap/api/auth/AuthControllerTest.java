package com.habitsnap.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitsnap.application.auth.AuthService;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;      // Java 객체 <-> JSON 변환

    @MockBean
    AuthService authService;


    // -------------------- SignUp (회원가입) ------------------------
    @Test
    @DisplayName("회원가입 성공 - 201 반환 + 서비스 singup() 호출")
    void signup_success() throws Exception {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .email("signup@habitsnap.com")
                .password("password1234!")
                .nickname("닉네임")
                .build();

        // authService.signup(request)는 void라고 가정
        Mockito.doNothing().when(authService).signUp(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())       // SecurityConfig에서 CSRF 켜져 있으면 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))     // ApiResponse 내부 코드/규칙에 맞게
                .andExpect(jsonPath("$.message").value("회원가입 성공"));

        // 회원가입 메서드 signup()이 1번 호출됐는지 검증
        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - @Valid 검증 실패로 400 반환 + GlobalExceptionHandler 포맷 검증")
    void singup_validation_fail_400() throws Exception {
        // given: 이메일 형식X, 비밀번호 8자 미만, 닉네임 비어있음 -> @Valid로 걸려야 함
        SignUpRequest invalidRequest = SignUpRequest.builder()
                .email("not-an-email")      // @Email 형식 위반
                .password("1234")           // min 8 위반
                .nickname("")               // @NotBlank 위반
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                // GlobalExceptionHandler가 ApiResponse 형태로 내려준다는 가정 하에 포맷 검증
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data").exists());

        // @Valid에서 막혔기 때문에 서비스는 호출되지 않아야 함
        verify(authService, times(0)).signUp(any(SignUpRequest.class));
    }



    // -------------------- Login (로그인) ------------------------
    @Test
    @DisplayName("로그인 성공 - 200 반환 + accessToken 응답")
    void login_success() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("login@habitsnap.com")
                .password("password1234~")
                .build();

        given(authService.login(any(LoginRequest.class)))
                .willReturn("mock-jwt-token");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())       // SecurityConfig에서 CSRF 켜져 있으면 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // 컨트롤러가 Map.of("accessToken", token) 반환하므로 JSON path는 $.accessToken
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"));
    }


    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 등으로 401 반환 + GlobalExceptionHandler 포맷까지 검증")
    void login_fail_401() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
                .email("login@habitsnap.com")
                .password("wrong-passwrod")
                .build();

        // 서비스에서 CustomException을 던진다고 가정
        // 예: USER_NOT_FOUND, INVALID_PASSWORD
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new CustomException(ErrorCode.INVALID_PASSWORD));


        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())       // SecurityConfig에서 CSRF 켜져 있으면 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // GlobalExceptionHandler 포맷 검증 (핵심)
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").exists());
                // 필요하면 상황에 맞게 더 구체적으로 가능
                /*.andExpect(jsonPath("$.code").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));*/

        verify(authService, times(1)).login(any(LoginRequest.class));
    }


}
