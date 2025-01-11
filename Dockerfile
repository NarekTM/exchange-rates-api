FROM gradle:jdk17 AS builder

WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon
COPY . .
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
