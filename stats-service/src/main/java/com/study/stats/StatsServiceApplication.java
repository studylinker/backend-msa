package com.study.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * stats-service 메인 클래스
 * - scanBasePackages = "com.study" 로 설정해서
 *   common-security, common-web 모듈의 @Component/@Configuration 도 함께 스캔
 */
@SpringBootApplication(scanBasePackages = "com.study")
public class StatsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatsServiceApplication.class, args);
    }

}