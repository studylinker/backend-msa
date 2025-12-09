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

// ===========================
// 2) 카테고리 비율 계산
// ===========================
public ChartResponse getMemberRatio() {

    String url = "http://user-service:10000/internal/users/stats";

    ResponseEntity<UserStatDTO[]> response =
            restTemplate.getForEntity(url, UserStatDTO[].class);

    UserStatDTO[] users = response.getBody();
    if (users == null || users.length == 0) {
        return new ChartResponse(
                List.of("데이터 없음"),
                List.of(0)
        );
    }

    Map<String, Integer> countMap = new HashMap<>();

    // 카테고리 합치기
    for (UserStatDTO user : users) {
        if (user.getCategories() == null) continue;

        for (String tag : user.getCategories()) {
            String key = tag.toLowerCase().trim();
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }
    }

    List<String> labels = new ArrayList<>(countMap.keySet());
    List<Integer> data = labels.stream()
            .map(countMap::get)
            .collect(Collectors.toList());

    return new ChartResponse(labels, data);
}
