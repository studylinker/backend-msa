package com.study.recommend.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TagRecommendResponse {

    private final RecommendCriteria criteria;
    private final double radiusKm;
    private final int limit;
    private final List<TagRecommendGroupDto> groups;
}
