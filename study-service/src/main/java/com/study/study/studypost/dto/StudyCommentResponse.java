package com.study.study.studypost.dto;

import com.study.study.studypost.domain.StudyComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyCommentResponse {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    // ✅ Entity -> DTO 변환
    public static StudyCommentResponse fromEntity(StudyComment comment) {
        return StudyCommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}