package com.study.user.controller;

import com.study.user.domain.User;
import com.study.user.dto.LoginVerifyRequest;
import com.study.user.dto.LoginVerifyResponse;
import com.study.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * auth-service에서 호출하는 내부 로그인 검증용 API
     * POST /internal/auth/verify-login
     * body: { "username": "...", "password": "..." }
     * 성공 시: 200 + userId/username/role 반환
     */
    @PostMapping("/verify-login")
    public ResponseEntity<LoginVerifyResponse> verifyLogin(
            @RequestBody LoginVerifyRequest request
    ) {
        // 1) username으로 유저 찾기
        User user = userService.getByUsernameOrThrow(request.getUsername());

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) 검증 성공 → auth-service에서 JWT 만들 수 있도록 최소 정보만 반환
        LoginVerifyResponse response = LoginVerifyResponse.fromUser(user);
        return ResponseEntity.ok(response);
    }
}