package com.study.study.studypost.service;

import com.study.study.studypost.domain.BoardType;
import com.study.study.studypost.domain.StudyComment;
import com.study.study.studypost.domain.StudyPost;
import com.study.study.studypost.domain.StudyReview;
import com.study.study.studypost.dto.*;
import com.study.study.studypost.repository.StudyCommentRepository;
import com.study.study.studypost.repository.StudyPostRepository;
import com.study.study.studypost.repository.StudyReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyPostService {

    private final StudyPostRepository postRepository;
    private final StudyReviewRepository reviewRepository;
    private final StudyCommentRepository commentRepository;

    // 🟡 문자열로 들어오는 studyDate 파싱용 포맷
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StudyPostService(
            StudyPostRepository postRepository,
            StudyReviewRepository reviewRepository,
            StudyCommentRepository commentRepository
    ) {
        this.postRepository = postRepository;
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
    }

    // ===================== 게시글 =====================

    // 전체 조회
    @Transactional(readOnly = true)
    public List<StudyPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(StudyPostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public StudyPostResponse getPost(Long postId) {
        StudyPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));
        return StudyPostResponse.fromEntity(post);
    }

    // ===================== 게시글 생성 =====================
    @Transactional
    public StudyPostResponse createPost(StudyPostCreateRequest request,
                                        Long requesterId,
                                        boolean isAdmin) {

        // 🟡 리더 ID 결정 로직 (DB User 엔티티 조회 없이 ID만 사용)
        Long leaderId;
        if (request.getLeaderId() == null) {
            // 리더 지정 안 했으면 본인
            leaderId = requesterId;
        } else {
            // 리더를 명시한 경우: 관리자이거나 본인만 허용
            if (!isAdmin && !request.getLeaderId().equals(requesterId)) {
                throw new SecurityException("리더는 로그인한 사용자 본인만 설정할 수 있습니다.");
            }
            leaderId = request.getLeaderId();
        }

        // ===============================
        // 📌 공지사항 전용 처리 (NOTICE)
        // ===============================
        if ("NOTICE".equalsIgnoreCase(request.getType())) {

            StudyPost notice = new StudyPost();
            notice.setLeaderId(leaderId);              // 🟡 User 엔티티 대신 leaderId
            notice.setTitle(request.getTitle());
            notice.setContent(request.getContent());
            notice.setType(BoardType.NOTICE);

            // 공지에는 필요 없는 값들을 기본값으로 세팅
            notice.setLocation("공지사항");
            notice.setMaxMembers(0);
            notice.setCurrentMembers(0);
            notice.setStudyDate(null);
            notice.setLatitude(null);
            notice.setLongitude(null);
            notice.setGroupId(request.getGroupId());   // 필요 시 그룹 ID만 보관

            StudyPost saved = postRepository.save(notice);
            return StudyPostResponse.fromEntity(saved);
        }

        // ===============================
        // 📌 일반 STUDY / FREE / REVIEW 생성 처리
        // ===============================
        StudyPost post = new StudyPost();
        post.setLeaderId(leaderId);                     // 🟡 User → leaderId
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());

        post.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 0);
        post.setCurrentMembers(0);

        if (request.getStudyDate() != null) {
            post.setStudyDate(LocalDateTime.parse(request.getStudyDate(), formatter));
        }

        if (request.getType() != null) {
            post.setType(BoardType.valueOf(request.getType().toUpperCase()));
        }

        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());

        // 🟡 스터디 그룹은 ID만 보관 (다른 서비스/엔티티 참조 없음)
        if (request.getGroupId() != null) {
            post.setGroupId(request.getGroupId());
        }

        StudyPost saved = postRepository.save(post);
        return StudyPostResponse.fromEntity(saved);
    }

    // ===================== 게시글 수정 =====================
    @Transactional
    public StudyPostResponse updatePost(Long postId,
                                        StudyPostUpdateRequest request,
                                        Long requesterId,
                                        boolean isAdmin) {

        StudyPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        // ⭐ 수정 :: 신고 요청을 가장 먼저 처리 → 권한 체크 건너뜀
        if (request.getReported() != null && request.getReported()) {
            post.setReported(true);
            post.setReportReason(request.getReportReason());
            post.setUpdatedAt(LocalDateTime.now());

            return StudyPostResponse.fromEntity(post);
        }
        // ⭐ 수정 끝

        Long writerId = post.getLeaderId();   // 🟡 기존 post.getLeader().getUserId() 대체

        // 작성자 또는 관리자만 수정 가능
        if (!writerId.equals(requesterId) && !isAdmin) {
            throw new SecurityException("게시글 작성자 또는 관리자만 수정할 수 있습니다.");
        }

        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getLocation() != null) post.setLocation(request.getLocation());
        if (request.getMaxMembers() != null) post.setMaxMembers(request.getMaxMembers());
        if (request.getStudyDate() != null) {
            post.setStudyDate(LocalDateTime.parse(request.getStudyDate(), formatter));
        }
        if (request.getType() != null) {
            post.setType(BoardType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getCurrentMembers() != null) {
            post.setCurrentMembers(request.getCurrentMembers());
        }
        if (request.getLatitude() != null) post.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) post.setLongitude(request.getLongitude());

        // 🟡 그룹 변경: groupId(Long)만 변경
        if (request.getGroupId() != null) {
            post.setGroupId(request.getGroupId());
        }

        // 🟡 신고 관련 필드 업데이트
        if (request.getReported() != null) {
            post.setReported(request.getReported());
        }
        if (request.getReportReason() != null) {
            post.setReportReason(request.getReportReason());
        }

        post.setUpdatedAt(LocalDateTime.now());
        return StudyPostResponse.fromEntity(post);
    }

    // ===================== 게시글 삭제 =====================
    @Transactional
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        StudyPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        Long writerId = post.getLeaderId();  // 🟡 User 엔티티 없이 ID 비교

        if (!writerId.equals(userId) && !isAdmin) {
            throw new SecurityException("게시글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // ===================== 리뷰 =====================
    @Transactional(readOnly = true)
    public List<StudyReviewResponse> getReviewsByPost(Long postId) {
        return reviewRepository.findByPostId(postId).stream()
                .map(StudyReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyReviewResponse createReview(Long postId,
                                            Long userId,
                                            StudyReviewCreateRequest request) {

        // 🟡 게시글 존재 여부만 확인 (엔티티 연관 안 걸고 검증만)
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        StudyReview review = new StudyReview();
        review.setPostId(postId);     // 🟡 post 엔티티 대신 postId
        review.setUserId(userId);     // 🟡 user 엔티티 대신 userId
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        StudyReview saved = reviewRepository.save(review);
        return StudyReviewResponse.fromEntity(saved);
    }

    @Transactional
    public StudyReviewResponse updateReview(Long postId,
                                            Long reviewId,
                                            Long userId,
                                            StudyReviewUpdateRequest request) {

        StudyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        // 🟡 postId / userId 기반으로 검증
        if (!review.getPostId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 리뷰가 아닙니다.");
        }

        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("리뷰 작성자만 수정할 수 있습니다.");
        }

        if (request.getContent() != null) review.setContent(request.getContent());
        if (request.getRating() != null) review.setRating(request.getRating());

        return StudyReviewResponse.fromEntity(review);
    }

    @Transactional
    public void deleteReview(Long postId,
                             Long reviewId,
                             Long userId,
                             boolean isAdmin) {

        StudyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + reviewId));

        if (!review.getPostId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 리뷰가 아닙니다.");
        }

        Long writerId = review.getUserId();   // 🟡 user 엔티티 없이 ID 비교

        if (!writerId.equals(userId) && !isAdmin) {
            throw new SecurityException("리뷰 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    // ===================== 댓글 =====================
    @Transactional(readOnly = true)
    public List<StudyCommentResponse> getCommentsByPost(Long postId) {

        // 🟡 게시글 존재 여부만 검증
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        List<StudyComment> comments =
                commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(StudyCommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyCommentCreate createComment(Long postId,
                                            Long userId,
                                            StudyCommentRequest request) {

        // 🟡 게시글 존재 여부만 검증
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));

        // 🟡 User 엔티티/리포지토리 없이, JWT에서 받은 userId 그대로 사용
        StudyComment comment = StudyComment.builder()
                .postId(postId)
                .userId(userId)
                .content(request.getContent())
                .build();

        StudyComment saved = commentRepository.save(comment);

        return StudyCommentCreate.builder()
                .message("댓글이 등록되었습니다.")
                .comment(StudyCommentResponse.fromEntity(saved))
                .build();
    }

    @Transactional
    public void deleteComment(Long postId,
                              Long commentId,
                              Long userId,
                              boolean isAdmin) {

        StudyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (!comment.getPostId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }

        Long writerId = comment.getUserId();  // 🟡 user 엔티티 없이 ID 비교

        if (!writerId.equals(userId) && !isAdmin) {
            throw new SecurityException("댓글 작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}
