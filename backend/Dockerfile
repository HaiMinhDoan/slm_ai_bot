# syntax=docker/dockerfile:1.7

# ---------- Giai đoạn build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml ./
COPY src ./src

# Cache mount cho kho Maven: không phải layer nên vẫn dùng lại được sau `docker compose build --no-cache`.
# Bỏ test lúc build image; chạy test riêng bằng ./mvnw test.
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q clean package -DskipTests

# ---------- Giai đoạn chạy ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# curl cho HEALTHCHECK, tzdata cho TZ
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && rm -rf /var/lib/apt/lists/*

# Chạy bằng người dùng thường, không phải root
RUN useradd --system --create-home --uid 10001 spring
USER spring

COPY --from=build --chown=spring:spring /src/target/slm-ai-*.jar app.jar

ENV TZ=Asia/Ho_Chi_Minh \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
