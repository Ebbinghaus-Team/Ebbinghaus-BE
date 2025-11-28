FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/*SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]

