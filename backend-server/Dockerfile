FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/exam-backend-1.0.0.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8000} --server.address=0.0.0.0"]
