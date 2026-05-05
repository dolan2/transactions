# Build stage — override toolchain to match available JDK
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN sed -i 's/JavaLanguageVersion.of(26)/JavaLanguageVersion.of(21)/' build.gradle
RUN ./gradlew dependencies --no-daemon --configuration runtimeClasspath 2>/dev/null || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
