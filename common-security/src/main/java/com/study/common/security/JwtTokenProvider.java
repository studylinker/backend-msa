package com.study.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key;

    // 간단한 로그아웃(토큰 블랙리스트) 구현용
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    private final long EXPIRATION_MS = 86400000L; // 1일

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String createToken(String username, String role, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        if (invalidatedTokens.contains(token)) {
            return false;  // 로그아웃된 토큰
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 username 추출
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    // 토큰에서 role 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 로그아웃용: 토큰 블랙리스트에 추가
    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
