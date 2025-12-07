package com.study.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {
        "com.study.study",          // 현재 서비스
        "com.study.common",          // 공통 모듈 전체 스캔 (security + web)
})
public class StudyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyServiceApplication.class, args);
    }
}
