package com.study.study.studypost.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyCommentRequest {
    private String content;
}