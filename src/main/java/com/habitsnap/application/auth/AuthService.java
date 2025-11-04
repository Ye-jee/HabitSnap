package com.habitsnap.application.auth;

import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.dto.auth.SignUpRequest;
import com.habitsnap.exception.CustomException;
import com.habitsnap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {  // 회원가입 로직

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void singUp(SignUpRequest request) {

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






}
