package com.study.study.studypost.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyCommentCreate {

    private String message;
    private StudyCommentResponse comment;
}