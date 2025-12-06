package com.study.recommend.repository;

import com.study.recommend.domain.UserInterestTag;
import com.study.recommend.domain.UserInterestTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInterestTagRepository
        extends JpaRepository<UserInterestTag, UserInterestTagId> {

    @Query("SELECT uit.tag FROM UserInterestTag uit WHERE uit.userId = :userId")
    List<String> findTagsByUserId(@Param("userId") Long userId);
}
