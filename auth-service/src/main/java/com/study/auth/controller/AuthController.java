package com.study.auth.controller;

import com.study.auth.dto.LoginRequest;
import com.study.auth.dto.TokenResponse;
import com.study.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ============================
    // POST /api/auth/tokens
    // 로그인(토큰 발급)
    // ============================
    @PostMapping("/tokens")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {

        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    // ============================
    // DELETE /api/auth/logout
    // 토큰 만료(로그아웃 처리)
    // ============================
   @PostMapping("/logout") // 👈 여기가 핵심입니다!
    public ResponseEntity<?> logout(HttpServletRequest request) {
        
        // 1. 헤더 확인 로그
        String header = request.getHeader("Authorization");
        System.out.println("=== [Controller] 로그아웃 요청 진입 ===");
        System.out.println("=== [Controller] 헤더 값: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("=== [Controller] 헤더 없음 또는 Bearer 아님 -> 400 반환 ===");
            return ResponseEntity.badRequest().body("Authorization 헤더가 없거나 형식이 틀립니다.");
        }

        // 2. 토큰 추출 및 서비스 호출
        String token = header.substring(7);
        System.out.println("=== [Controller] 토큰 추출 완료: " + token);
        
        authService.logout(token);
        
        System.out.println("=== [Controller] 서비스 호출 완료, 200 OK 반환 ===");
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}

