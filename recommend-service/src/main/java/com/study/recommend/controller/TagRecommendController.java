package com.study.recommend.controller;

import com.study.common.security.JwtTokenProvider;
import com.study.recommend.dto.TagRecommendResponse;
import com.study.recommend.service.TagRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class TagRecommendController {

    private final TagRecommendService tagRecommendService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/tag")
    public TagRecommendResponse getTagBasedRecommend(
            HttpServletRequest request,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "5") double radiusKm,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "0.5") double alpha,
            @RequestParam(required = false, defaultValue = "0.5") double beta
    ) {

        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        Long userId = jwtTokenProvider.getUserId(token);

        return tagRecommendService.getTagBasedGroups(
                userId, lat, lng, radiusKm, limit, alpha, beta
        );
    }
}
