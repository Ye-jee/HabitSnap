package com.habitsnap.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity       // @PreAuthorize 등 메서드 단위 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // JWT 기반 인증 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 방식이므로 세션, 폼로그인, CSRF 모두 비성활화
                .csrf(csrf -> csrf.disable())           // CSRF 비활성화
                .formLogin(login -> login.disable())    // 기본 로그인 폼 비활성화
                .httpBasic(basic -> basic.disable())    // Basic 인증도 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        /*.anyRequest().permitAll()   // 모든 요청 허용*/
                        .requestMatchers(
                                "/api/auth/**",     // 로그인/회원가입
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/test/**"              // 테스트용 엔드포인트
                        ).permitAll()
                        .anyRequest().authenticated()       // 나머지는 인증 필요
                )

                // JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager 빈 등록 (패스워드 인코더 등 인증 과정에 사용)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

}
