package com.study.system.config;

import com.study.common.security.JwtAuthenticationFilter;
import com.study.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// CORS 관련
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * system-service 보안 설정
 * - JWT 기반 리소스 서버
 * - /api/system/** 는 ADMIN 권한 필요
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 토큰 유틸 (common-security 쪽에 있다고 가정)
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JwtAuthenticationFilter 빈을 직접 등록
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // JWT 사용하므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // 세션 사용 안 함 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 엔드포인트별 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 헬스 체크 & 루트 경로는 공개
                        .requestMatchers("/", "/health", "/actuator/health", "/favicon.ico").permitAll()

                        // 내부 서비스 간 호출용 (필요 시 사용)
                        .requestMatchers("/internal/**").permitAll()

                        // 시스템 운영용 API - ADMIN 전용
                        .requestMatchers("/api/system/**").hasRole("ADMIN")

                        // 나머지는 전부 막기
                        .anyRequest().denyAll()
                );

        // 여기서 우리가 @Bean으로 만든 jwtAuthenticationFilter() 사용
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // TODO: 실제 프론트 도메인으로 변경
        config.setAllowedOrigins(List.of(
                "https://gachon.studylink.click",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}