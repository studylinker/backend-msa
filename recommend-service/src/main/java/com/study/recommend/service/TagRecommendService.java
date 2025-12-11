package com.study.recommend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.recommend.dto.RecommendCriteria;
import com.study.recommend.dto.TagRecommendGroupDto;
import com.study.recommend.dto.TagRecommendResponse;
import com.study.recommend.repository.TagLocationProjection;
import com.study.recommend.repository.TagLocationRepository;
import com.study.recommend.repository.UserInterestTagRepository;
import com.study.recommend.util.DistanceScoreUtil;
import com.study.recommend.util.TagSimilarityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagRecommendService {

    private final TagLocationRepository tagLocationRepository;
    private final UserInterestTagRepository userInterestTagRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TagRecommendResponse getTagBasedGroups(
            Long userId,
            double userLat,
            double userLng,
            double radiusKm,
            int limit,
            double alpha,
            double beta
    ) {
        // 1. 유저 관심 태그 조회
        List<String> userTags = userInterestTagRepository.findTagsByUserId(userId);
        if (userTags == null) {
            userTags = List.of();
        }
        final List<String> finalUserTags = userTags;

        // 2. 반경 내 그룹 조회
        List<TagLocationProjection> candidates =
                tagLocationRepository.findGroupsByLocation(userLat, userLng, radiusKm, limit);

        // 3. 가중치 normalize
        double sum = alpha + beta;
        if (sum <= 0) {
            alpha = 0.5;
            beta = 0.5;
            sum = 1.0;
        }
        final double weightAlpha = alpha / sum;
        final double weightBeta = beta / sum;

        // 4. 스코어 계산 + 정렬 + limit
        List<TagRecommendGroupDto> groups = candidates.stream()
                .map(p -> {
                    double distanceKm = p.getDistanceKm() != null ? p.getDistanceKm() : Double.MAX_VALUE;
                    double distanceScore = DistanceScoreUtil.calculateDistanceScore(distanceKm);

                    List<String> groupTags = parseJsonArrayToList(p.getCategory());

                    // 1) 사용자 태그 & 그룹 태그 정규화
                    List<String> userNorm = TagSimilarityUtil.normalizeTags(finalUserTags);
                    List<String> groupNorm = TagSimilarityUtil.normalizeTags(groupTags);

                    // 2) 정규화된 태그로 유사도 계산
                    double tagSimilarity = TagSimilarityUtil.jaccardSimilarity(userNorm, groupNorm);

                    // 최종 점수 계산 (기존 동일)
                    double finalScore = distanceScore * weightAlpha + tagSimilarity * weightBeta;


                    return TagRecommendGroupDto.builder()
                            .studyGroupId(p.getGroupId())
                            .name(p.getTitle())
                            .category(groupTags)
                            .latitude(p.getLatitude())
                            .longitude(p.getLongitude())
                            .distanceKm(distanceKm)
                            .distanceScore(distanceScore)
                            .tagSimilarity(tagSimilarity)
                            .finalScore(finalScore)
                            .build();
                })
                .sorted(Comparator.comparing(TagRecommendGroupDto::getFinalScore).reversed())
                .limit(limit)
                .toList();

        // 5. 래퍼 DTO 반환.
        return TagRecommendResponse.builder()
                .criteria(RecommendCriteria.TAG_LOCATION)
                .radiusKm(radiusKm)
                .limit(limit)
                .groups(groups)
                .build();
    }

    private List<String> parseJsonArrayToList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
