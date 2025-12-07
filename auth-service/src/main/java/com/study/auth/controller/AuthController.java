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
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization 헤더가 없습니다.");
        }

        String token = header.substring(7); // "Bearer " 제거
        authService.logout(token); // 👉 서비스에 위임

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
