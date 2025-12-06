package com.study.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PopularLocationResponse {

    private RecommendCriteria criteria;
    private double radiusKm;
    private int limit;
    private List<PopularLocationGroupDto> groups;
}
