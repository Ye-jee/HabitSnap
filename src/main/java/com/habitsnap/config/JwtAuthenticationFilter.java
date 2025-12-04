package com.habitsnap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.habitsnap.common.response.ApiResponse;
import com.habitsnap.domain.user.User;
import com.habitsnap.domain.user.UserRepository;
import com.habitsnap.exception.ApiErrorResponse;
import com.habitsnap.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.Collections;

/* JWT 인증 필터 (CustomUserDetails 기반)
* - Authorization 헤더에서 JWT 토큰을 추출하고
* - 유효하면 UserRepository에서 사용자 정보를 조회하여
* - SecurityContext에 CustomUserDetails 등록
* */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음 또는 Bearer 형식 아님");
            filterChain.doFilter(request, response);
            return;
        }

        // 1) 토큰 주출
        String token = authHeader.substring(7);      // “Bearer “ 제거
        log.debug("Authorization 헤더 파싱 성공 : {}", token);

        try {
            // 2) 토큰 검증
            if(jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                log.debug("JWT 검증 성공 - 사용자 이메일: {}", email);

                // 3) SecurityContextHolder에 인증 정보 등록 <- *이 부분 수정*
                // 3-1) UserRepository를 이용해 사용자 조회
                User user = userRepository.findByEmail(email)
                        .orElseThrow(()-> new RuntimeException("해당 이메일의 사용자를 찾을 수 없습니다."));

                // 3-2) CustomUserDetails 생성
                CustomUserDetails customUserDetails = new CustomUserDetails(user);

                // 3-3) 인증 객체 생성 및 SecurityContext 등록
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                );
                /*UsernamePasswordAuthenticationToken  authenticationToken = new UsernamePasswordAuthenticationToken(
                        new User(email, "", Collections.emptyList()), // 단순 유저 객체
                        null,
                        Collections.emptyList()
                );*/

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            /*else {
                log.warn("JWT 유효성 검증 실패");
            }*/

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료 예외 발생");
            handleJwtException(response, ErrorCode.EXPIRED_TOKEN, e);
            return;     // 이후 체인 중단

        } catch (SignatureException e) {
            handleJwtException(response, ErrorCode.INVALID_SIGNATURE, e);
            return;

        } catch (JwtException | IllegalArgumentException e) {
            handleJwtException(response, ErrorCode.INVALID_TOKEN, e);
            return;
        }


        // SecurityContext에 저장된 인증 정보 확인 로그 (디버깅용)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("[JWT 필터] SecurityContext 인증 주체 타입: {}", principal.getClass().getSimpleName());
            log.info("[JWT 필터] SecurityContext 인증 주체 내용: {}", principal);
        }


        // 4) 다음 필터로 요청 전달 - 정상 토큰이거나 비인증 요청이면 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // JWT 예외 발생 시 일관된 JSON 응답 반환
    private void handleJwtException(HttpServletResponse response, ErrorCode errorCode, Exception e) throws IOException {
        log.warn("JWT 검증 실패 : {}", e.getMessage());

        // Http 응답 상태 및 타입 지정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        /*ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();*/

        // ApiResponse로 실패 응답 생성
        ApiResponse<Void> errorResponse = ApiResponse.fail(errorCode);

        new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValue(response.getWriter(), errorResponse);
    }

}
