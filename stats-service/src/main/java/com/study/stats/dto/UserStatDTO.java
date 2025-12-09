package com.study.stats.dto;

import lombok.Data;
import java.util.List;

/**
 * user-service 의 /internal/users/stats 응답 DTO 복제본
 */
@Data
public class UserStatDTO {

    private Long userId;
    private List<String> categories;
}
