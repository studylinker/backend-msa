package com.study.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor; // 추가됨
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate; // 추가됨
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit; // 추가됨

@Component
@RequiredArgsConstructor // 생성자 주입 자동화 (RedisTemplate 받기 위해)
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key key;

    // [수정 1] 메모리 Set 삭제 -> RedisTemplate 추가
    private final StringRedisTemplate redisTemplate;

    private final long EXPIRATION_MS = 86400000L; // 1일

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성 (변경 없음)
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
        // [수정 2] Redis에 있는지 확인 (있으면 로그아웃된 토큰)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            return false; 
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

    // ... getUsername, getUserId, getRole 등은 변경 없음 ...
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // [수정 3] 로그아웃: Redis에 남은 시간만큼만 저장 (자동 삭제됨)
    public void invalidateToken(String token) {
        long remainTime = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        
        if (remainTime > 0) {
            redisTemplate.opsForValue().set(token, "logout", remainTime, TimeUnit.MILLISECONDS);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
