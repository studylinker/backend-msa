# 1. Base Image: 가볍고 안정적인 OpenJDK 17 사용
FROM openjdk:17-jdk-slim

# 2. Argument: CI/CD에서 넘겨준 서비스 이름 (예: auth-service)
# 워크플로우의 build-args: SERVICE_NAME=... 부분이 여기로 들어옵니다.
ARG SERVICE_NAME

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. JAR 파일 복사
# GitHub Actions가 이미 'mvn package'를 실행했으므로 target 폴더에 jar가 있습니다.
# 예: ./auth-service/target/*.jar -> /app/app.jar 로 복사
COPY ${SERVICE_NAME}/target/*.jar app.jar

# 5. (선택사항) 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul

# 6. 실행 명령어
# 어떤 서비스든 내부에서는 app.jar로 이름이 통일되었으므로 실행 명령어도 동일합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
