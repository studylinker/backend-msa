package com.study.study.studypost.controller;

import com.study.common.security.JwtUserInfo;
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

    private boolean isLoggedIn(JwtUserInfo user) {
        return user != null;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    // 🔍 디버깅용 공통 로그 메서드
    private void logDebug(String endpoint, JwtUserInfo user, String extra) {
        System.out.println("[StudyPostController] " + endpoint);
        if (user == null) {
            System.out.println("  - user: null (NOT AUTHENTICATED)");
        } else {
            System.out.println("  - userId: " + user.getUserId());
            System.out.println("  - username: " + user.getUsername());
            System.out.println("  - isAdmin: " + user.isAdmin());
        }
        if (extra != null && !extra.isEmpty()) {
            System.out.println("  - extra: " + extra);
        }
    }

    // ==================== 게시글 API ====================

    // GET /api/study-posts
    @GetMapping
    public ResponseEntity<?> getAllPosts(
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        List<StudyPostResponse> list = studyPostService.getAllPosts();
        return ResponseEntity.ok(list);
    }

    // GET /api/study-posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        StudyPostResponse post = studyPostService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    // POST /api/study-posts
    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyPostCreateRequest request
    ) {
        if (!isLoggedIn(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        StudyPostResponse created =
                studyPostService.createPost(request, userId, admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PATCH /api/study-posts/{postId}
    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyPostUpdateRequest request
    ) {
        // 🔍 디버깅 로그
        logDebug("PATCH /api/study-posts/" + postId, user,
                "title=" + request.getTitle());

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        // 🔍 디버깅 로그
        logDebug("DELETE /api/study-posts/" + postId, user, null);

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        List<StudyReviewResponse> list = studyPostService.getReviewsByPost(postId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{postId}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyReviewCreateRequest request
    ) {
        // 🔍 디버깅 로그
        logDebug("POST /api/study-posts/" + postId + "/reviews", user,
                "rating=" + request.getRating());

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyReviewUpdateRequest request
    ) {
        // 🔍 디버깅 로그
        logDebug("PATCH /api/study-posts/" + postId + "/reviews/" + reviewId, user,
                "rating=" + request.getRating());

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        // 🔍 디버깅 로그
        logDebug("DELETE /api/study-posts/" + postId + "/reviews/" + reviewId, user, null);

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        // 🔍 디버깅 로그
        logDebug("GET /api/study-posts/" + postId + "/comments", user, null);

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestBody StudyCommentRequest request
    ) {
        // 🔍 디버깅 로그
        logDebug("POST /api/study-posts/" + postId + "/comments", user,
                "content=" + request.getContent());

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
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
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        // 🔍 디버깅 로그
        logDebug("DELETE /api/study-posts/" + postId + "/comments/" + commentId, user, null);

        if (!isLoggedIn(user)) {
            System.out.println("  -> BLOCKED: not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long userId = user.getUserId();
        boolean admin = isAdmin(user);

        studyPostService.deleteComment(postId, commentId, userId, admin);
        return ResponseEntity.noContent().build();
    }
}