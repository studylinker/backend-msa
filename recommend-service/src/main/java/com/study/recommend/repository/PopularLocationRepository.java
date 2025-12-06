package com.study.recommend.repository;

import com.study.recommend.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularLocationRepository extends JpaRepository<StudyGroup, Long> {

    @Query(value = """
        SELECT
            sg.group_id       AS groupId,
            sg.title          AS title,
            sg.description    AS description,
            COUNT(gm.user_id) AS memberCount,
            sg.max_members    AS maxMembers,
            sg.status         AS status,
            sg.latitude       AS latitude,
            sg.longitude      AS longitude,
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
        LEFT JOIN Group_members gm
            ON sg.group_id = gm.group_id
            AND gm.status = 'APPROVED'
        WHERE
            sg.status = 'ACTIVE'
            AND sg.latitude IS NOT NULL
            AND sg.longitude IS NOT NULL
        GROUP BY
            sg.group_id, sg.title, sg.description, sg.max_members,
            sg.status, sg.latitude, sg.longitude
        HAVING
            distanceKm <= :radiusKm
        ORDER BY
            memberCount DESC,
            distanceKm ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<PopularLocationProjection> findPopularGroupsByLocation(
            @Param("userLat") double userLat,
            @Param("userLng") double userLng,
            @Param("radiusKm") double radiusKm,
            @Param("limit") int limit
    );
}
