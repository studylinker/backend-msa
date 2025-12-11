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
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate; // [추가]
    

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                   StringRedisTemplate redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("--- [JWT FILTER] Request URI: " + request.getRequestURI() + " ---");

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            // 401 Unauthorized 에러 반환하고 필터 종료
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Logged out token."); 
            return; // 더 이상 진행하지 않음
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
