package com.study.recommend.controller;

import com.study.recommend.dto.PopularLocationResponse;
import com.study.recommend.service.PopularLocationService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class PopularRecommendController {

    private final PopularLocationService popularLocationService;

    /**
     * 인기 + 위치 기반 스터디 추천함
     * GET /api/recommend/popular?lat=..&lng=..&radiusKm=..&limit=..&popWeight=..&distanceWeight=..
     */
    @GetMapping("/popular")
    public PopularLocationResponse getPopularGroups(
            @RequestParam("lat") double latitude,
            @RequestParam("lng") double longitude,
            @RequestParam(value = "radiusKm", required = false) Double radiusKm,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "popWeight", required = false) Double popWeight,
            @RequestParam(value = "distanceWeight", required = false) Double distanceWeight
    ) {
        return popularLocationService.getPopularGroupsByLocation(
                latitude, longitude, radiusKm, limit, popWeight, distanceWeight
        );
    }
}
