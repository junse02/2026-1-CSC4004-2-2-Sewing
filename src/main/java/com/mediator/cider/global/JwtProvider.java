package com.mediator.cider.global;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 발급하는 클래스
 * 작성자: 성준서
 */

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long validityInMilliseconds;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.expiration}") long validityInMilliseconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes()); // 비밀키 생성
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 토큰 생성
    public String createToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email) // 토큰 주체 (이메일)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key) // 우리만의 비밀키로 서명!
                .compact();
    }

    // 1. 토큰에서 이메일 추출
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key) // 우리 비밀키로 복호화 준비
                .build()
                .parseSignedClaims(token) // 토큰 해석
                .getPayload()
                .getSubject(); // 이메일 반환
    }

    // 2. 토큰 유효성 검사 (만료 여부 등)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 토큰이 변조되었거나 만료된 경우 false
            return false;
        }
    }
}