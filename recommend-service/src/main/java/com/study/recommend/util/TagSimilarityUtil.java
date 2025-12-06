package com.study.recommend.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagSimilarityUtil {

    /**
     * Jaccard Similarity = |Intersection| / |Union|
     */
    public static double jaccardSimilarity(List<String> userTags, List<String> groupTags) {
        if (userTags == null || groupTags == null || userTags.isEmpty() || groupTags.isEmpty()) {
            return 0.0;
        }

        Set<String> userSet = new HashSet<>(userTags);
        Set<String> groupSet = new HashSet<>(groupTags);

        Set<String> intersection = new HashSet<>(userSet);
        intersection.retainAll(groupSet);

        Set<String> union = new HashSet<>(userSet);
        union.addAll(groupSet);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }
}
