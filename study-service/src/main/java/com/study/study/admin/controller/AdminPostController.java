package com.study.study.admin.controller;

import com.study.common.security.JwtUserInfo;
import com.study.study.studypost.dto.StudyPostCreateRequest;
import com.study.study.studypost.dto.StudyPostResponse;
import com.study.study.studypost.dto.StudyPostUpdateRequest;
import com.study.study.studypost.service.StudyPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final StudyPostService studyPostService;

    public AdminPostController(StudyPostService studyPostService) {
        this.studyPostService = studyPostService;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    // ⭐ 1) 관리자용 게시글 전체 조회
    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal JwtUserInfo user) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body("관리자만 조회할 수 있습니다.");
        }
        List<StudyPostResponse> list = studyPostService.getAllPosts();
        return ResponseEntity.ok(list);
    }

    // ⭐ 2) 공지사항 등록
    @PostMapping("/notice")
    public ResponseEntity<?> createNotice(
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyPostCreateRequest request
    ) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body("관리자만 공지를 생성할 수 있습니다.");
        }

        request.setType("NOTICE");
        StudyPostResponse created =
                studyPostService.createPost(request, user.getUserId(), true);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ⭐ 3) 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> delete(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body("관리자만 삭제할 수 있습니다.");
        }

        studyPostService.deletePost(postId, user.getUserId(), true);

        return ResponseEntity.noContent().build();
    }

    // ⭐ 4) 신고 사유 포함 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<?> getOne(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body("관리자만 상세 조회할 수 있습니다.");
        }

        return ResponseEntity.ok(studyPostService.getPost(postId));
    }

    // ⭐ 5) 관리자용 신고 처리 (reported 값 수정)
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updateReported(
            @PathVariable Long postId,
            @RequestBody StudyPostUpdateRequest request,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body("관리자만 수정할 수 있습니다.");
        }

        return ResponseEntity.ok(
                studyPostService.updatePost(postId, request, user.getUserId(), true)
        );
    }
}
