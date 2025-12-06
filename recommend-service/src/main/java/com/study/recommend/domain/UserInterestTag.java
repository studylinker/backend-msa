package com.study.recommend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "User_interest_tags")
@IdClass(UserInterestTagId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterestTag {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "tag")
    private String tag;
}
