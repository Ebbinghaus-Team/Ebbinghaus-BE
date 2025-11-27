FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]
