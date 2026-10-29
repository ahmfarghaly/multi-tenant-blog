# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY gateway/pom.xml gateway/
COPY blog-service/pom.xml blog-service/
RUN mvn dependency:go-offline -B -pl blog-service -am || true

COPY blog-service/src blog-service/src
RUN mvn clean package -DskipTests -pl blog-service -am

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /workspace/blog-service/target/blog-service-*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
