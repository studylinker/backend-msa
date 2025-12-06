package com.study.stats.repository;

import com.study.studygroup.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRatioRepository extends JpaRepository<StudyGroup, Long> {

    @Query(
            value = "SELECT category, COUNT(*) FROM Study_groups GROUP BY category",
            nativeQuery = true
    )
    List<Object[]> getCategoryRatio();
}
