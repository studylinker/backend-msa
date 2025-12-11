package com.study.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authorization 헤더에서 JWT를 꺼내 검증하고,
 * 유효하면 JwtUserInfo를 principal로 SecurityContext에 저장하는 공통 필터
 *
 * ⚠️ UserDetailsService, DB 조회 없음 (토큰만 신뢰).
 */

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    // [추가] 인터페이스 주입 (Redis 모름)
    private final TokenBlacklistService tokenBlacklistService;

    // [생성자 1] Auth-Service용: 블랙리스트 구현체를 주입받음
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // [생성자 2] User/System-Service용: 블랙리스트 검사 안 함 (기본값 false)
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = (token) -> false; // 무조건 통과
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // System.out.println("--- [JWT FILTER] Request URI: " + request.getRequestURI() + " ---");

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // [추가된 로직] 블랙리스트(로그아웃 된 토큰)인지 확인
        // 만약 블랙리스트라면 -> 인증 없이 그냥 통과시킴 (결국 뒤에서 401 남)
        if (tokenBlacklistService.isBlacklisted(token)) {
            System.out.println("--- [JWT FILTER] Blacklisted Token Detected! ---");
            filterChain.doFilter(request, response);
            return;
        }

        // --- 기존 로직 그대로 유지 ---
        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String username = jwtTokenProvider.getUsername(token);
        String role = jwtTokenProvider.getRole(token);

        JwtUserInfo principal = new JwtUserInfo(userId, username, role);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        token,   // ⭐⭐ 여기!!! JWT를 credential 에 저장
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
