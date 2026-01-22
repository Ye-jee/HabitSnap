package com.habitsnap.application.auth;

import com.habitsnap.config.JwtTokenProvider;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.dto.auth.LoginRequest;
import com.habitsnap.dto.auth.SignUpRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {  // 회원가입 로직 + 로그인 로직

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인 로직
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void signUp(SignUpRequest request) {

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        userRepository.save(user);
    }


    // 로그인, jwt 토큰
    public String login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // JWT 생성
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // ✅ 여기에서 info 로그 추가
        log.info("✅ 로그인 성공 - email: {}, 발급된 토큰: {}", user.getEmail(), token);

        return token;
    }






}
