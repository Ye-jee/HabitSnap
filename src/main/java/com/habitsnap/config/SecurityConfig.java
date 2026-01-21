package com.habitsnap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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

    /* ObjectMapper를 Bean으로 등록해서
    * - 필터/EntryPoint/예외핸들러 어디서든 같은 설정으로 JSON 응답을 만들 수 있게 한다.
    * - LocalDateTime 같은 JavaTime 직렬화 문제도 방지한다.
    * */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // JWT 기반 인증 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // REST API 방식이므로 세션, 폼로그인, CSRF 모두 비성활화 + Stateless
                .csrf(csrf -> csrf.disable())           // CSRF 비활성화
                .formLogin(login -> login.disable())    // 기본 로그인 폼 비활성화
                .httpBasic(basic -> basic.disable())    // Basic 인증도 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별(URL) 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        /*.anyRequest().permitAll()   // 모든 요청 허용*/
                        .requestMatchers(
                                "/api/auth/**",     // 로그인/회원가입
                                "/api/upload/**",            // 업로드 테스트용
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/test/**"              // 테스트용 엔드포인트
                        ).permitAll()
                        .anyRequest().authenticated()       // 나머지는 인증 필요
                )

                // 인증 실패는 401을 보장하도록 EntryPoint 명시, 예외 처리(여기가 401/403의 "최종 결론"을 내리는 부분)
                /* 인증 실패(401) 응답을 항상 JSON(ApiResponse.fail)로 통일
                * - JwtAuthenticationFilter가 실패 원인을 request attribute에 심어두면
                * 여기서 그 값을 읽어 정확한 ErrorCode로 응답할 수 있도록 한다.
                * */
                .exceptionHandling(ex -> ex

                    // 인증 실패(로그인 안됨) -> 401
                    .authenticationEntryPoint((request, response, authException) -> {
                        // 필터가 기록해둔 JWT 실패 사유를 우선 사용
                        Object attr = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTR);

                        ErrorCode errorCode;
                        if(attr instanceof ErrorCode){
                            errorCode = (ErrorCode) attr;
                        }
                        else {
                            // 기록된 사유가 없으면 "토큰 자체가 없음"으로 본다
                            errorCode = ErrorCode.MISSING_TOKEN;
                        }

                        response.setStatus(errorCode.getStatus().value());
                        // response.setStatus(401);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");

                        ApiResponse<Void> body = ApiResponse.fail(errorCode);

                        objectMapper().writeValue(response.getWriter(), body);
                    })

                    // 인가 실패(권한 부족) -> 403 (추후 ADMIN 생기면 사용 예정)
                    /*.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");

                        // 권한 부족 전용 ErrorCode가 있으면 그걸 쓰는 게 제일 좋음
                        // 없으면 일단 ACCESS_DENIED 같은 코드를 새로 만드는 걸 추천
                        ApiResponse<Void> body = ApiResponse.fail(ErrorCode.ACCESS_DENIED);

                        objectMapper.writeValue(response.getWriter(), body);
                    })*/

                )

                // JWT 인증 필터 등록 (UsernamePasswordAuthenticationFilter 전에 넣어야 함)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager 빈 등록 (AuthService 로그인 처리, 패스워드 인코더 등 인증 과정에 사용)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

}
