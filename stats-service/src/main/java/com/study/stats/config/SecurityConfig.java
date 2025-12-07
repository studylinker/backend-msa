package com.study.stats.config;

import com.study.common.security.JwtAuthenticationFilter;
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
 * stats-service 보안 설정
 * - JWT 기반 리소스 서버 (로그인 X)
 * - /api/stats/** 는 ADMIN 권한 필요
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 이 서비스에서 직접 로그인 처리하진 않지만, 혹시 모를 용도 대비
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // JWT 사용하므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // 세션 사용 안함 (완전 Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 헬스 체크 및 루트는 공개
                        .requestMatchers("/actuator/health", "/health", "/", "/favicon.ico").permitAll()

                        // 내부용 API 있으면 여기서 permitAll 또는 별도 규칙 지정
                        .requestMatchers("/internal/**").permitAll()

                        // 운영 대시보드 통계는 ADMIN 전용
                        .requestMatchers("/api/stats/**").hasRole("ADMIN")

                        // 그 외는 모두 인증 필요 (혹은 denyAll 도 가능)
                        .anyRequest().authenticated()
                )
                // UsernamePasswordAuthenticationFilter 전에 JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * - 프론트에서 호출 가능하도록 Origin/Method/Header 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // TODO: 실제 프론트 주소들로 교체 (배포 시 도메인 추가)
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}