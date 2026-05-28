FROM gradle:8.10.2-jdk21-alpine AS build

WORKDIR /workspace

COPY gradlew gradle settings.gradle build.gradle ./
COPY server ./server

RUN chmod +x gradlew && ./gradlew :server:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/server/build/libs/service-center-server.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
