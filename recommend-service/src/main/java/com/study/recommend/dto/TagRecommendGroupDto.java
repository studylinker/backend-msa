package com.study.recommend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagRecommendGroupDto {

    private Long studyGroupId;
    private String name;

    private List<String> category;

    private Double latitude;
    private Double longitude;

    private double distanceKm;
    private double distanceScore;
    private double tagSimilarity;
    private double finalScore;
}
