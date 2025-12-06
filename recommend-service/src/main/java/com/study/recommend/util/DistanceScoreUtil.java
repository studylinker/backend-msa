package com.study.recommend.util;

public class DistanceScoreUtil {

    /**
     * 거리(km)에 따른 점수: 1 / (1 + distanceKm)
     * 가까울수록 점수 ↑
     */
    public static double calculateDistanceScore(double distanceKm) {
        if (distanceKm < 0) {
            return 0.0;
        }
        return 1.0 / (1.0 + distanceKm);
    }
}
