package com.habitsnap.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/* JWT + Spring Security 통합 테스트
* - 목적: JwtAuthenticationFilter + SecurityConfig의 EntryPoint가 실제 요청 흐름에서 '의도한 방식으로' 동작하는지 검증한다.
* - 핵심포인트: JWT 인증/예외 흐름 통합 검증 (컨트롤러 로직 테스트 X)
* */

@ActiveProfiles("test")     // application-test.yml 사용
@SpringBootTest             // 실제 SecurityFilterChain까지 포함
@AutoConfigureMockMvc       // MockMvc로 HTTP 요청 시뮬레이션
public class JwtSecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // JwtTokenProvider에서 쓰는 것과 같은 시크릿을 테스트에서도 사용해서 "정상 서명/만료 토큰"을 만들 수 있음
    @Value("${jwt.secret}")
    private String jwtSecret;

    /* 보호된 API를 하나 잡고(= authenticated() 필요한 경로),
    * 토큰 유무/형태/만료/위조 등에 따라 SecurityFilter(JWT 필터)가 어떻게 응답하는지 검증한다.
    * 여기서는 예시로 MealRecordController의 날짜 조회를 사용
    * GET /api/meals/date?value=2025-12-02
    * */
    private static final String PROTECTED_URL = "/api/meals/date?value=2025-12-02";

    // ------------------- 1) 인증 실패: 토큰 없음 ------------------------
    @Test
    @DisplayName("인증 실패 - '토큰 없이' 보호된 API 요청하면, 401 반환")
    void unauthorized_when_no_token() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized());

        /* 여기서 바디(JSON)까지 강하게 단정하지 않는 이유/status(401) 검증만 하는 이유:
         * - 토큰이 아예 없으면 JwtAuthenticationFilter는 '아무것도 안 하고' 지나감
         * - 이후 Spring Security가 anonymous 접근을 차단하면서 401을 내는데,
         *   이 401 응답 바디(JSON)/Content-Type은 프로젝트의 EntryPoint 설정에 따라 달라질 수 있음
         */
    }


    // ------------------- 2) JWT 예외: Authorization 헤더 형식 오류 -------------------
    @Test
    @DisplayName("JWT 예외 - Authorization 헤더가 Bearer로 시작하지 않으면 401 + JSON(ApiResponse.fail) 반환")
    void unauthorized_when_invalid_auth_header() throws Exception {
        /* 해당 테스트가 보장/검증하는 것
        * - Authorization 헤더 형식이 잘못되면 JWT 필터 및 EntryPoint 조합에 의해,
        * 401 + ApiResponse.fail(INVALID_AUTH_HEADER) 형태로 응답된다.
        * */

        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Token abc.def.ghi"))  // Bearer가 아닌 잘못된 Authorization 헤더
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ApiResponse 공통 포맷 검증 + ErrorCode 검증
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.code").value("INVALID_AUTH_HEADER"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

    }



    // ------------------- 3) JWT 예외: 만료 토큰 -------------------
    @Test
    @DisplayName("JWT 예외 - 만료 토큰이면 401 + JSON(ApiResponse.fail(EXPIRED_TOKEN)) 반환")
    void unauthorized_when_expired_token() throws Exception {
        /* 해당 테스트의 핵심:
        * - exp가 과거인 JWT -> JwtTokenProvider에서 ExpiredJwtException 발생
        * - JwtAuthenticationFilter는 인증 정보 설정X
        * - Security EntryPoint가 401 + EXPIRED_TOKEN 반환
        * */

        String expiredToken = createExpiredToken("expired@habitsnap.com");

        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ApiResponse 공통 포맷 검증 + ErrorCode 검증
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.code").value("EXPIRED_TOKEN"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

    }



    // ------------------- 4) JWT 예외: 위조/서명 불일치 토큰 -------------------
    @Test
    @DisplayName("JWT 예외 - 서명 불일치(다른 시크릿으로 서명한 토큰)면 401 + JSON(ApiResponse.fail(INVALID_SIGNATURE)) 반환")
    void unauthorized_when_invalid_signature_token() throws Exception {
        /* 해당 테스트 핵심:
        * - 서버 시크릿과 다른 시크릿으로 서명
        * - validation()에서 SignatureException 발생
        * - 인증 실패 -> 401 + INVALID_SIGNATURE 반환
        * */

        String forgedToken = createTokenWithDifferentSecret("forged@habitsnap.com");

        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ApiResponse 공통 포맷 검증 + ErrorCode 검증
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.code").value("INVALID_SIGNATURE"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());

    }

    // JWT 토큰 생성 헬퍼

    /* JwtTokenProvider와 동일한 secret로 "이미 만료된 토큰" 생성
     * - exp를 과거로 세팅해서 parseClaimsJws 시 ExpiredJwtException을 유발
     */
    private String createExpiredToken(String email){
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        Instant now = Instant.now();
        Date issuedAt = Date.from(now.minusSeconds(300));
        Date expiredAt = Date.from(now.minusSeconds(100));   // 100초 전에 이미 만료됨(60초 스큐를 초과함)

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    /* "다른 secret"로 서명한 토큰 생성 -> 서버 secret로 검증 시 서명 불일치로 실패
     */
    private String createTokenWithDifferentSecret(String email) {
        // 길이 유지 중요: 원래 시크릿과 비슷한 길이를 유지해서 HS256 조건 충족
        String otherSecret = jwtSecret + "_DIFFERENT_DIFFERENT_DIFFERENT";
        byte[] keyBytes = otherSecret.getBytes(StandardCharsets.UTF_8);

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date exp = Date.from(now.plusSeconds(60 * 30));

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)       // HS256 명시
                .compact();
    }


}
