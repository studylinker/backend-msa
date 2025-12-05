package com.study.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 비밀번호 인코더 (AuthService에서 쓰는 것)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // JWT, REST API라서 CSRF 꺼버림
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // ✅ auth-service의 로그인/로그아웃는 인증 필요 없음
                        .requestMatchers("/api/auth/**").permitAll()

                        // 나머지는 일단 막아두기
                        .anyRequest().authenticated()
                );

        // 여기서는 아직 JWT 필터 안 붙임 (토큰 검증은 study-service에서 하도록)
        return http.build();
    }
}