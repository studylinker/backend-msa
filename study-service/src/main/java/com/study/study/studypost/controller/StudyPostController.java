package com.study.study.studypost.controller;

import com.study.common.security.JwtUserInfo; // 🟡 JwtUserInfo 추가

import com.study.study.studypost.dto.*;
import com.study.study.studypost.service.StudyPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/study-posts")
public class StudyPostController {

    private final StudyPostService studyPostService;

    public StudyPostController(StudyPostService studyPostService) {
        this.studyPostService = studyPostService;
    }

    // ==================== 유틸 메서드 ====================

    private boolean isLoggedIn(JwtUserInfo user) { // 🟡 타입 변경
        return user != null;
    }

    private boolean isAdmin(JwtUserInfo user) { // 🟡 타입 변경
        return user != null && user.isAdmin();
    }

    // ==================== 게시글 API ====================

    // GET /api/study-posts
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        List<StudyPostResponse> list = studyPostService.getAllPosts();
        return ResponseEntity.ok(list);
    }

    // GET /api/study-posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        StudyPostResponse post = studyPostService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    // POST /api/study-posts
    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody StudyPostCreateRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();   // 🟡 JwtUserInfo 방식
        boolean admin = isAdmin(user);    // 🟡 관리자 여부 판단

        StudyPostResponse created =
                studyPostService.createPost(request, userId, admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PATCH /api/study-posts/{postId}
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody StudyPostUpdateRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        StudyPostResponse updated =
                studyPostService.updatePost(postId, request, userId, admin);

        return ResponseEntity.ok(updated);
    }

    // DELETE /api/study-posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        studyPostService.deletePost(postId, userId, admin);
        return ResponseEntity.noContent().build();
    }

    // ==================== 리뷰 API ====================

    @GetMapping("/{postId}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        List<StudyReviewResponse> list = studyPostService.getReviewsByPost(postId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{postId}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody StudyReviewCreateRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        StudyReviewResponse created =
                studyPostService.createReview(postId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{postId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody StudyReviewUpdateRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();

        StudyReviewResponse updated =
                studyPostService.updateReview(postId, reviewId, userId, request);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        studyPostService.deleteReview(postId, reviewId, userId, admin);
        return ResponseEntity.noContent().build();
    }

    // ==================== 댓글 API ====================

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        List<StudyCommentResponse> list =
                studyPostService.getCommentsByPost(postId);

        return ResponseEntity.ok(list);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody StudyCommentRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        StudyCommentCreate created =
                studyPostService.createComment(postId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        studyPostService.deleteComment(postId, commentId, userId, admin);
        return ResponseEntity.noContent().build();
    }
}
