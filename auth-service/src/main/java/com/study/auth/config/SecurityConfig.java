package com.study.auth.config;

import com.study.auth.service.RedisTokenBlacklistService; // [추가]
import com.study.common.security.JwtAuthenticationFilter; // [추가]
import com.study.common.security.JwtTokenProvider;       // [추가]
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // [추가] final 필드 주입용
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    // 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // TODO: 배포 시 실제 프론트 주소들로 교체
        config.setAllowedOrigins(List.of("http://gachon.studylink.click:80", "http://localhost:3000")); // localhost 추가 권장
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. 헬스체크 허용
                        .requestMatchers("/actuator/health", "/health", "/", "/favicon.ico","/actuator/prometheus").permitAll()

                        // 2. 로그인(Login)은 누구나 접근 가능해야 함 -> permitAll
                        .requestMatchers("/api/auth/login", "/api/auth/verify-login").permitAll()
                        
                        // 3. [중요] 로그아웃(Logout)은 토큰이 있는 사람만 해야 함 -> authenticated 권장
                        // (하지만 필터에서 토큰 검사 후 블랙리스트 처리를 하므로 permitAll로 두고 필터에 맡겨도 동작은 함)
                        .requestMatchers("/api/auth/logout").authenticated()

                        // 4. 나머지 모든 요청 허용 (auth-service 특성상)
                        .anyRequest().permitAll()
                )
                // [핵심 변경] 이제 로그아웃 처리를 위해 필터를 추가합니다.
                // 로그인 시에는 토큰이 없으므로 필터가 아무 동작 안 하고 통과시킵니다. (안전함)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, redisTokenBlacklistService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
