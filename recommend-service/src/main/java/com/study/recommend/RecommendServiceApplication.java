package com.study.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.study.recommend",      // 현재 recommendation-service
                "com.study.common.security", // JwtTokenProvider, Filter
                "com.study.common.web"       // WebConfig 등
        }
)
public class RecommendServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendServiceApplication.class, args);
    }
}
