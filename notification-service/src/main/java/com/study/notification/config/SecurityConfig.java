package com.study.notification.config;

import com.study.common.security.JwtAuthenticationFilter; // ★ common-security 패키지명에 맞게 수정
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // JWT API 서비스는 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 세션을 사용하지 않는 Stateless 전략
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 알림 서비스는 로그인 기능이 없음 → 모든 요청 JWT 필수
                        .requestMatchers("/api/notifications/**").authenticated()
                        // 그 외 요청도 모두 차단
                        .anyRequest().authenticated()
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
