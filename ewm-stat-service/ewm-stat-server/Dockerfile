FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/ewm-stat-server.jar /app/ewm-stat-server.jar
EXPOSE 9090
CMD ["java", "-jar", "ewm-stat-server.jar"]
