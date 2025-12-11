package com.study.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * system-service 메인 클래스
 * - scanBasePackages = "com.study" 로 설정해서
 *   common-security, common-web 까지 모두 스캔되도록 함
 */
@SpringBootApplication(scanBasePackages = "com.study",
                       excludeName = {
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
        })
public class SystemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }

}
