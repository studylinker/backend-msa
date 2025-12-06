package com.study.recommend.repository;

import com.study.recommend.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagLocationRepository extends JpaRepository<StudyGroup, Long> {

    @Query(value = """
        SELECT
            sg.group_id    AS groupId,
            sg.title       AS title,
            sg.category    AS category,
            sg.latitude    AS latitude,
            sg.longitude   AS longitude,
            (
                6371 * ACOS(
                    COS(RADIANS(:userLat))
                    * COS(RADIANS(sg.latitude))
                    * COS(RADIANS(sg.longitude) - RADIANS(:userLng))
                    + SIN(RADIANS(:userLat))
                    * SIN(RADIANS(sg.latitude))
                )
            ) AS distanceKm
        FROM Study_groups sg
        WHERE
            sg.status = 'ACTIVE'
            AND sg.latitude IS NOT NULL
            AND sg.longitude IS NOT NULL
        HAVING
            distanceKm <= :radiusKm
        ORDER BY
            distanceKm ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<TagLocationProjection> findGroupsByLocation(
            @Param("userLat") double userLat,
            @Param("userLng") double userLng,
            @Param("radiusKm") double radiusKm,
            @Param("limit") int limit
    );
}
