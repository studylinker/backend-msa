package com.study.recommend.domain;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserInterestTagId implements Serializable {

    private Long userId;
    private String tag;
}
