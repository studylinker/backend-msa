package com.study.study; // 🔥 이거 꼭 맞춰줘야 함 (com.study.study 패키지)

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.study.study",   // 현재 서비스
        "com.study.common"   // common-security, common-web 등
})
@EnableJpaRepositories(basePackages = "com.study.study")  // JPA Repository 스캔
@EntityScan(basePackages = "com.study.study")              // @Entity 스캔
public class StudyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyServiceApplication.class, args);
    }
}