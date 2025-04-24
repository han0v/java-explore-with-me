FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/ewm-main-service-0.0.1-SNAPSHOT.jar /app/ewm-main-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar",  "-Dspring.profiles.active=docker", "ewm-main-service.jar"]
