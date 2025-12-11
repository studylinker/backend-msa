package com.study.recommend.util;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class TagSimilarityUtil {

    //동의어 / 약어 / 한국어 → 영어 매핑
    private static final Map<String, String> SYNONYM_MAP = Map.ofEntries(

            // ===== 프로그래밍 언어 =====
            Map.entry("py", "python"),
            Map.entry("python3", "python"),
            Map.entry("파이썬", "python"),

            Map.entry("js", "javascript"),
            Map.entry("자바스크립트", "javascript"),

            Map.entry("ts", "typescript"),
            Map.entry("타입스크립트", "typescript"),

            Map.entry("c언어", "c"),
            Map.entry("씨언어", "c"),

            Map.entry("c++", "cpp"),
            Map.entry("cpp", "cpp"),
            Map.entry("씨뿔뿔", "cpp"),

            // ===== 프론트엔드 =====
            Map.entry("reactjs", "react"),
            Map.entry("reactnative", "react"),
            Map.entry("react.js", "react"),
            Map.entry("리액트", "react"),

            Map.entry("vuejs", "vue"),
            Map.entry("뷰", "vue"),

            // ===== 백엔드 =====
            Map.entry("springboot", "spring"),
            Map.entry("스프링부트", "spring"),
            Map.entry("스프링", "spring"),

            Map.entry("nodejs", "node"),
            Map.entry("node.js", "node"),
            Map.entry("노드", "node"),

            // ===== AI / 머신러닝 =====
            Map.entry("ml", "machinelearning"),
            Map.entry("머신러닝", "machinelearning"),

            Map.entry("dl", "deeplearning"),
            Map.entry("딥러닝", "deeplearning"),

            Map.entry("ai", "artificialintelligence"),
            Map.entry("인공지능", "artificialintelligence"),

            // ===== 데이터베이스 =====
            Map.entry("db", "database"),
            Map.entry("데이터베이스", "database"),

            // ===== CS 일반 =====
            Map.entry("알고리즘", "algorithm"),
            Map.entry("algorithms", "algorithm"),

            Map.entry("자료구조", "datastructure"),
            Map.entry("data-structure", "datastructure")
    );

    // 규칙 기반 정규화
    public static String normalizeTag(String tag) {
        if (tag == null) return "";

        // 1) 소문자 변환
        String t = tag.toLowerCase();

        // 2) 공백/특수문자 제거 ("react.js" → "reactjs")
        t = t.replaceAll("[^a-z0-9가-힣]", "");

        // 3) 한글 자모 결합 ("머신 러닝" → "머신러닝")
        t = Normalizer.normalize(t, Normalizer.Form.NFC);

        // 4) 복수형 제거 (tags → tag)
        if (t.endsWith("s") && t.length() > 1) {
            t = t.substring(0, t.length() - 1);
        }

        return t;
    }

    //동의어 매핑
    public static String applySynonym(String tag) {
        return SYNONYM_MAP.getOrDefault(tag, tag);
    }

    // 최종 정규화
    public static String normalizeFull(String tag) {
        String t = normalizeTag(tag);
        return applySynonym(t);
    }

    //리스트 정규화
    public static List<String> normalizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(TagSimilarityUtil::normalizeFull)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 🔥 Jaccard Similarity (기존 그대로)
     */
    public static double jaccardSimilarity(List<String> a, List<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;

        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) return 0.0;

        return (double) intersection.size() / union.size();
    }
}
