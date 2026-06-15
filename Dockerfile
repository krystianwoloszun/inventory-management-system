# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper and build scripts first to leverage layer caching
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# Copy sources and build the executable jar
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Run as an unprivileged user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
