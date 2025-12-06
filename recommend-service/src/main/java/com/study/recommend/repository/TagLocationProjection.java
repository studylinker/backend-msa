package com.study.recommend.repository;

public interface TagLocationProjection {

    Long getGroupId();
    String getTitle();
    String getCategory();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceKm();
}
