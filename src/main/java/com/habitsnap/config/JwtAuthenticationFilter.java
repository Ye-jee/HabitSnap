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

/* [개발 및 운영 관점]
 * 역할:
 * - Authorization 헤더에서 "Bearer {token}"만 처리
 * - 토큰이 유효하면 사용자 조회 후 SecurityContext에 Authentication 세팅
 * - 토큰이 없거나(비인증 요청) / 토큰이 이상하면(인증 실패) => "응답 생성하지 않고" 그냥 다음 필터로 넘김
 *
 *  응답을 만들지 않는 이유:
 * - 필터에서 응답을 직접 만들기 시작하면, 예외 케이스/포맷/상태코드 정책이 필터마다 흩어져서 유지보수 난이도가 올라감
 * - 운영에서는 보통 "AuthenticationEntryPoint"에서 401 응답 정책을 일관되게 처리하는 편이 안정적임
 *
 * - 인증 정보 설정만 담당하고 응답 생성을 하지 않음
 * - 실패 사유는 request attribute에 기록하고,
 *      최종 401 응답(JSON)은 SecurityConfig의 AuthenticationEntryPoint에서 생성
 * - 모든 분기마다 debug 로그 추가 -> 테스트/개발/운영 디버깅에 용이
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // EntryPoint에서 읽어갈 "JWT 실패 사유" 표식 키 (키 문자열은 한 곳에서 관리하는게 안전)
    public static final String JWT_ERROR_ATTR = "jwt_error_code";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("[JWT] 필터 진입 - URI: {}, Method: {}",
                request.getRequestURI(), request.getMethod());

        // 0) 이미 인증이 들어간 요청이면 굳이 다시 토큰 파싱/DB 조회할 필요 없음, 즉 이미 인증된 요청이면 스킵
        if(SecurityContextHolder.getContext().getAuthentication() != null){
            log.debug("[JWT] 이미 SecurityContext에 인증 정보 존재 -> 필터 스킵");
            filterChain.doFilter(request, response);
            return;
        }

        // 1) Authorization 헤더 확인
        String authHeader = request.getHeader("Authorization");

        // 1-1) 헤더가 없거나 Bearer 형식이 아니면 => "비인증 요청"으로 보고 패스
        // (예: /api/auth/login, /api/auth/signup 같은 공개 API, 혹은 토큰 없이 접근 가능한 엔드포인트)
        if (authHeader == null) {
            log.debug("[JWT] Authorization 헤더 없음 -> 비인증 요청으로 처리");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.debug("[JWT] Authorization 헤더가 Bearer 형식 아님: {}", authHeader);
            markJwtError(request, ErrorCode.INVALID_AUTH_HEADER);

            filterChain.doFilter(request, response);
            return;
        }

        // 2) 토큰 주출 ("Bearer " 제거)
        String token = authHeader.substring(7).trim();      // "Bearer " 제거
        log.debug("[JWT] Bearer 토큰 추출 완료");

        if (token.isEmpty()){
            // "Bearer "까지만 있고 토큰이 비어있으면 인증 불가 => 컨텍스트 비우고 패스
            log.debug("[JWT] Bearer 뒤에 토큰이 비어 있음 -> 인증 불가");
            markJwtError(request, ErrorCode.INVALID_TOKEN);

            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("Authorization 헤더 파싱 성공 : {}", token);

        try {
            // 3) 토큰 유효성 검증 (서명/만료/형식 등)
            log.debug("[JWT] 토큰 유효성 검증 시작");

            boolean valid = jwtTokenProvider.validateToken(token);

            if(!valid) {
                // 토큰이 이상하면 인증정보를 만들지 않고 패스
                log.debug("[JWT] 토큰 유효성 검증 실패 -> 인증 정보 설정 안 함");
                markJwtError(request, ErrorCode.INVALID_TOKEN);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("[JWT] 토큰 유효성 검증 성공");

            // 4) 토큰에서 email(subject 등) 꺼내기
            String email = jwtTokenProvider.getEmailFromToken(token);
            log.debug("[JWT] 토큰에서 추출한 email: {}", email);

            // email이 null 이나 blank이면 DB에서 "email is null" 같은 이상 쿼리가 나갈 수 있으니 차단
            if (email == null || email.isBlank()) {
                log.debug("[JWT] email이 null 또는 blank -> 인증 실패 처리");
                markJwtError(request, ErrorCode.INVALID_TOKEN);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 5) 토큰이 유효할 때만 DB 조회 (중요*)
            log.debug("[JWT] 사용자 조회 시도 - email: {}", email);

            User user = userRepository.findByEmail(email).orElse(null);

            if(user == null){
                // 운영에서는 여기서 예외를 던져서 500 에러를 만들기 보다는 "인증 실패"로 보고 컨텍스트를 비우고 패스하는 게 안전함
                /* 토큰은 유효하지만 사용자 데이터가 없는 경우(탈퇴/DB 불일치 등)
                    운영 관점에서는 보통 401로 처리하는 편이 자연스러움 */
                log.warn("[JWT] 토큰은 유효하지만 사용자 조회 실패 - email: {}", email);
                markJwtError(request, ErrorCode.USER_NOT_FOUND);

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("[JWT] 사용자 조회 성공 - userId: {}", user.getId());

            // 6) SecurityContext에 인증 객체(인증 정보) 등록
            CustomUserDetails principal = new CustomUserDetails(user);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.debug("[JWT] SecurityContext에 인증 정보 등록 완료");

        } catch (ExpiredJwtException e) {
            // 만료 토큰: 인증 불가 => 컨텍스트 비우고 패스
            // 만료는 "정상적인 실패 이벤트"에 가까워서 info()로 작성함
            log.info("[JWT] 만료된 토큰 - {}", e.getMessage());
            markJwtError(request, ErrorCode.EXPIRED_TOKEN);

            SecurityContextHolder.clearContext();

        } catch (SignatureException e) {
            // 서명 불일치: 인증 불가
            log.warn("[JWT] 토큰 서명 불일치 - {}", e.getMessage());
            markJwtError(request, ErrorCode.INVALID_SIGNATURE);

            SecurityContextHolder.clearContext();

        } catch (JwtException | IllegalArgumentException e) {
            // 파싱 실패/형식 오류 등: 인증 불가
            log.warn("[JWT] 토큰 파싱/검증 실패 - {}", e.getMessage());
            markJwtError(request, ErrorCode.INVALID_TOKEN);

            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            // 예상치 못한 예외도 필터에서 응답 만들지 말고, "인증정보"만 비우고 넘기기
            log.error("[JWT] 예상치 못한 예외 발생", e);
            markJwtError(request, ErrorCode.INVALID_TOKEN);

            SecurityContextHolder.clearContext();
        }

        // SecurityContext에 저장된 인증 정보 확인 로그 (디버깅용)
        /*if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("[JWT 필터] SecurityContext 인증 주체 타입: {}", principal.getClass().getSimpleName());
            log.info("[JWT 필터] SecurityContext 인증 주체 내용: {}", principal);
        }*/

        // 7) 다음 필터로 진행
        log.debug("[JWT] 필터 종료 -> 다음 필터로 요청 전달");
        filterChain.doFilter(request, response);
    }

    // JWT 실패 사유를 request attribute에 기록해 두면, 이후에 AuthenticationEntryPoint가 이를 읽어 "정확한 ErrorCode"로 응답할 수 있다.
    private void markJwtError(HttpServletRequest request, ErrorCode errorCode) {
        request.setAttribute(JWT_ERROR_ATTR, errorCode);
        log.debug("[JWT] 실패 사유 기록: {}", errorCode.name());
    }

    // JWT 예외 발생 시 일관된 JSON 응답 반환
    /*private void handleJwtException(HttpServletResponse response, ErrorCode errorCode, Exception e) throws IOException {
        log.warn("JWT 검증 실패 : {}", e.getMessage());

        // Http 응답 상태 및 타입 지정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        *//*ApiErrorResponse apiErrorResponse = ApiErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();*//*

        // ApiResponse로 실패 응답 생성
        ApiResponse<Void> errorResponse = ApiResponse.fail(errorCode);

        new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValue(response.getWriter(), errorResponse);
    }*/

}
