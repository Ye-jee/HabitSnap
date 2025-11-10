package com.habitsnap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.Collections;

/* JWT 인증 필터
    - 매 요청마다 Authorization 헤더의 JWT를 검증
    - 유효하면 SecurityContext에 인증 정보 등록
* */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

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
        String token = authHeader.substring(7);
        log.debug("Authorization 헤더 파싱 성공 : {}", token);

        try {
            // 2) 토큰 검증
            if(jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                log.debug("JWT 검증 성공 - 사용자 : {}", email);

                // 3) SecurityContextHolder에 인증 정보 등록
                UsernamePasswordAuthenticationToken  authenticationToken = new UsernamePasswordAuthenticationToken(
                        new User(email, "", Collections.emptyList()), // 단순 유저 객체
                        null,
                        Collections.emptyList()
                );

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

        // 4) 다음 필터로 요청 전달 - 정상 토큰이거나 비인증 요청이면 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // JWT 예외 발생 시 일관된 JSON 응답 반환
    private void handleJwtException(HttpServletResponse response, ErrorCode errorCode, Exception e) throws IOException {
        log.warn("JWT 검증 실패 : {}", e.getMessage());

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();

        new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValue(response.getWriter(), apiErrorResponse);
    }

}
