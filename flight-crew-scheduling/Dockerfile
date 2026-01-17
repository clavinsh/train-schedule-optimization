FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -Dquarkus.package.type=fast-jar

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built application
COPY --from=build /app/target/quarkus-app /app

# Set log level to INFO
ENV QUARKUS_LOG_LEVEL=INFO

EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]
