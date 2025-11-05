package com.habitsnap.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.security.SignatureException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 토큰 생성, 검증, 정보 추출을 담당하는 클래스
@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expiration-ms}") long accessTokenValidityInMs,
            @Value("${jwt.issuer:HabitSnap}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
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


    // 토큰 유효성 검증
    public boolean validateToken(String token){

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)     // 1분 오차 허용
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료 : {}", e.getMessage());
        }catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 형식 : {}", e.getMessage());
        }catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 구조 : {}", e.getMessage());
        }catch (SecurityException | SignatureException e) {
            log.warn("JWT 서명 검증 실패 ; {}", e.getMessage());
        }catch (IllegalArgumentException e) {
            log.warn("JWT 토큰 값이 비어 있거나 잘못됨 : {}", e.getMessage());
        }

        return false;
    }


    // 토큰에서 이메일(Subject) 추출
    public String getEmailFromToken(String token) {
        try {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();

        } catch (JwtException e) {
            log.error("JWT 파싱 중 오류 발생 : {}", e.getMessage());
            return null;
        }
    }



}
