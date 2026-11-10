# Stage 1: Build the application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application with a lightweight JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from stage 1
COPY --from=build /app/target/exam-backend-1.0.0.jar app.jar

# Expose port (Render will inject $PORT automatically via ENV variable)
EXPOSE 8000

# Start command
ENTRYPOINT ["java", "-jar", "app.jar"]
