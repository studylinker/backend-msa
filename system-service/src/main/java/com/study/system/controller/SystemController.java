package com.study.system.controller;

import com.study.common.security.JwtUserInfo;
import com.study.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시스템 운영(백업, 캐시 클리어 등) 컨트롤러
 * - /api/system/** 는 ADMIN 권한 필요.
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    // 🔥 백업 스냅샷 생성
    @PostMapping("/backup")
    public ResponseEntity<String> createBackup(@AuthenticationPrincipal JwtUserInfo principal) {
        // 어떤 관리자가 실행했는지 기록하고 싶으면 여기서 사용
        Long adminId = principal.getUserId();
        String adminRole = principal.getRole();

        systemService.createBackup(adminId);
        return ResponseEntity.ok("Backup snapshot process executed.");
    }

    // 🔥 캐시 무효화
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache(@AuthenticationPrincipal JwtUserInfo principal) {
        Long adminId = principal.getUserId();

        systemService.clearCache(adminId);
        return ResponseEntity.ok("Cache clear executed.");
    }
}
