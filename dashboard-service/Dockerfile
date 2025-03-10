# OpenJDK 21 기반의 경량 Alpine 이미지를 사용
FROM eclipse-temurin:21-jdk-alpine

# 빌드된 JAR 파일을 컨테이너 내부에 복사 (ARG로 빌드 시 전달)
ARG JAR_FILE=build/libs/dashboard-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 외부에 노출할 포트 설정 (예: 8888)
EXPOSE 8888

# 컨테이너 실행 시 JAR 파일을 실행
ENTRYPOINT ["java", "-jar", "app.jar"]