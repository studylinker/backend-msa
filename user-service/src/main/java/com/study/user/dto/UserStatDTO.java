package com.study.user.dto;

import java.util.List;

public class UserStatDTO {

    private Long userId;
    private List<String> categories;

    public UserStatDTO() {}

    public UserStatDTO(Long userId, List<String> categories) {
        this.userId = userId;
        this.categories = categories;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getCategories() {
        return categories;
    }
}
