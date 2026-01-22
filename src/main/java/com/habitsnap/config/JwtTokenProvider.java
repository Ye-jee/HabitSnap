package com.habitsnap.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.security.SignatureException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

// JWT 토큰 생성, 검증, 정보 추출을 담당하는 클래스
@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMs;
    private final String issuer;

    // validation(), getEmail() 모두 동일하게 사용할 clock skew
    private static final long CLOCK_SKEW_SECONDS = 60;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expiration-ms}") long accessTokenValidityInMs,
            @Value("${jwt.issuer:HabitSnap}") String issuer
    ) {
        // charset 명시
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.issuer = issuer;
    }


    // Access Token 생성
    public String generateToken(String email){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityInMs);

        return Jwts.builder()
                .setSubject(email)              // 토큰 주체(사용자 식별)
                .setIssuer(issuer)              // 발급자 명시
                .setIssuedAt(now)               // 발급 시각
                .setExpiration(expiry)          // 만료 시각
                .signWith(key, SignatureAlgorithm.HS256)    // 서명
                .compact();
    }

    // 파싱 로직을 하나로 통일
    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS)
                // issuer 검증까지 하고 싶으면 아래 코드 추가 (운영에서 꽤 유용)
                // .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // 토큰 유효성 검증
    public boolean validateToken(String token){

        // 여기서 예외가 나면 필터가 잡아서 401 처리하도록 던지는 전략도 가능
        // 지금은 boolean 유지하되, 만료 포함 예외를 그대로 던지면 더 깔끔함
        parseClaims(token);
        return true;

        /*try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)     // 1분 오차 허용
                    .build()
                    .parseClaimsJws(token);

            return true;        // 검증 성공 시 true

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료 : {}", e.getMessage());
            throw e;    // 만료 예외는 그대로 던져서 필터에서 처리
        }catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 형식 : {}", e.getMessage());
        }catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 구조 : {}", e.getMessage());
        }catch (SecurityException | SignatureException e) {
            log.warn("JWT 서명 검증 실패 ; {}", e.getMessage());
        }catch (IllegalArgumentException e) {
            log.warn("JWT 토큰 값이 비어 있거나 잘못됨 : {}", e.getMessage());
        }

        return false;   // 만료 외의 경우는 false 반환*/
    }


    // 토큰에서 이메일(Subject) 추출
    public String getEmailFromToken(String token) {

        // null 반환 금지: 파싱 실패하면 예외를 그대로 던져서 필터가 처리하도록 함
        Claims claims = parseClaims(token);
        return claims.getSubject();

        /*try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();

        } catch (JwtException e) {
            log.error("JWT 파싱 중 오류 발생 : {}", e.getMessage());
            return null;
        }*/
    }



}
