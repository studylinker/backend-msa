package com.study.auth.service;

import com.study.common.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 👉 임시 로그인 로직
     *  - DB 연동 없이, 하드코딩 계정으로만 로그인 허용
     *  - username: admin, password: 1234 인 경우에만 성공
     */
    public String login(String username, String password) {

        System.out.println("[AuthService] username=" + username + ", password=" + password);

        // TODO: 나중에 진짜 DB 연동으로 교체
        if (!"admin".equals(username) || !"1234".equals(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        // 🔐 JWT 토큰 생성
        return jwtTokenProvider.createToken(
                username,
                "ADMIN",  // 임시로 ADMIN 권한
                1L        // 임시 유저 ID
        );
    }

    /**
     * 👉 로그아웃 (현재는 토큰 무효화만 호출)
     */
    public void logout(String token) {
        jwtTokenProvider.invalidateToken(token);
    }
}
