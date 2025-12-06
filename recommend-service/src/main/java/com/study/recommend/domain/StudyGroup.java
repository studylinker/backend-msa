package com.study.recommend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Study_groups")
@Getter
@NoArgsConstructor
public class StudyGroup {

    @Id
    @Column(name = "group_id")
    private Long groupId;

    private String title;

    @Column(columnDefinition = "mediumtext")
    private String description;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(columnDefinition = "longtext")
    private String category;

    // 🔥 precision/scale 절대 넣으면 안 됨!!
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private GroupStatus status;

    public enum GroupStatus {
        PENDING, ACTIVE, INACTIVE, REJECTED
    }
}
