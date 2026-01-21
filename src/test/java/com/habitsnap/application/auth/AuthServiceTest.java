package com.habitsnap.application.auth;

import com.habitsnap.config.JwtTokenProvider;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    // -------------------- SignUp (회원가입) ------------------------
    @Test
    @DisplayName("회원가입 성공: 이메일 중복이 없으면 비밀번호 인코딩 후 저장함")
    void signUp_success() {
        // given - 입력과 의존성 동작을 '가정'으로 설정
        SignUpRequest request = mock(SignUpRequest.class);

        when(request.getEmail()).thenReturn("test@habitsnap.com");
        when(request.getPassword()).thenReturn("rawPw");
        when(request.getNickname()).thenReturn("닉네임");

        // false을 반환해 존재하지 않는 이메일이라 회원가입 성공
        when(userRepository.existsByEmail("test@habitsnap.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPw")).thenReturn("encodedPw");

        // when - 실제 서비스 메서드 호출
        authService.signUp(request);

        // then - 의존성이 제대로 호출됐는지, 저장된 값이 맞는지 확인 및 검증
        verify(userRepository).existsByEmail("test@habitsnap.com");
        verify(passwordEncoder).encode("rawPw");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@habitsnap.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPw");
        assertThat(saved.getNickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("회원가입 실패: 이메일 중복이면 DUPLICATE_EMAIL 예외 발생")
    void signUp_fail_duplicateEmail() {
        // given
        SignUpRequest request = mock(SignUpRequest.class);

        when(request.getEmail()).thenReturn("dup@habitsnap.com");

        // true를 반환해 중복된 이메일이라 회원가입 실패
        when(userRepository.existsByEmail("dup@habitsnap.com")).thenReturn(true);

        // when - 커스텀 예외 발생 시 검증
        CustomException exception = catchThrowableOfType(
                () -> authService.signUp(request),
                CustomException.class
        );

        // then - 예외가 발생했는지를 확인 및 검증
        assertThat(exception).isNotNull();

        // CustomException이 errorCode getter를 제공한다면(보통 getErrorCode())
        // assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // getter가 없다면 메시지로라도 확인(최소 보장)
        // 예외 내용 확인 및 검증
        assertThat(exception.getMessage()).contains(ErrorCode.DUPLICATE_EMAIL.getMessage());

        verify(userRepository).existsByEmail("dup@habitsnap.com");
        // 이번엔 호출이 되지 않았는지를 확인 및 검증
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }


    // -------------------- Login (로그인) ------------------------
    @Test
    @DisplayName("로그인 성공: 비밀번호 일치 시 JWT 토큰 발급")
    void login_success() {
        // given
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("login@habitsnap.com");
        when(request.getPassword()).thenReturn("rawPw");

        User user = User.builder()
                .email("login@habitsnap.com")
                .password("encodedPw")
                .nickname("로그인유저")
                .build();

        when(userRepository.findByEmail("login@habitsnap.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPw","encodedPw")).thenReturn(true);
        when(jwtTokenProvider.generateToken("login@habitsnap.com")).thenReturn("jwt-token");

        // when
        String token = authService.login(request);

        // then
        assertThat(token).isEqualTo("jwt-token");

        verify(userRepository).findByEmail("login@habitsnap.com");
        verify(passwordEncoder).matches("rawPw", "encodedPw");
        verify(jwtTokenProvider).generateToken("login@habitsnap.com");

    }

    @Test
    @DisplayName("로그인 실패: 이메일이 없으면 USER_NOT_FOUND 예외 발생")
    void login_fail_userNotFound() {
        // given
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("none@habitsnap.com");

        when(userRepository.findByEmail("none@habitsnap.com")).thenReturn(Optional.empty());

        // when - 커스텀 예외 발생 시 검증
        CustomException exception = catchThrowableOfType(
                () -> authService.login(request),
                CustomException.class
        );

        // then - 예외가 발생했는지와 예외 내용 확인 및 검증
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findByEmail("none@habitsnap.com");
        // 호출이 되지 않았는지를 확인 및 검증
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호가 틀리면 INVALID_PASSWORD 예외 발생")
    void login_fail_invalidPassword() {
        // given
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("login@habitsnap.com");
        when(request.getPassword()).thenReturn("wrongPw");

        User user = User.builder()
                .email("login@habitsnap.com")
                .password("encodedPw")
                .nickname("로그인유저")
                .build();

        when(userRepository.findByEmail("login@habitsnap.com")).thenReturn(Optional.of(user));
        // 비밀번호 맞지 않는 결과를 내기 위해 false 리턴
        when(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false);

        // when - 커스텀 예외 발생 시 검증
        CustomException ex = catchThrowableOfType(
                () -> authService.login(request),
                CustomException.class
        );

        // then - 예외가 발생했는지와 예외 내용 확인 및 검증
        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains(ErrorCode.INVALID_PASSWORD.getMessage());

        verify(userRepository).findByEmail("login@habitsnap.com");
        verify(passwordEncoder).matches("wrongPw", "encodedPw");
        // 토큰을 만들어지는 메서드가 호출되지 않았는지를 확인 및 검증
        verify(jwtTokenProvider, never()).generateToken(any());
    }


}
