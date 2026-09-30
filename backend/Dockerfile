# ==========================================
# Multi-Stage Production Dockerfile (Spring Boot 3 / Java 17)
# ==========================================

# 1. Build Stage
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Copy Gradle files for dependency caching
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download dependencies
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# Copy source code and build production bootJar
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2. Production Runtime Stage
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Create a secure non-root system user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built JAR from builder stage
COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar app.jar

# Environment defaults
ENV SERVER_PORT=8081 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8081

# Liveness/Readiness Health Check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8081/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
