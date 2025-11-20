FROM eclipse-temurin:25-jre-alpine
LABEL authors="eckofox"

#sets working directory to /app
WORKDIR /app

#copies build from build/directory to /app
COPY /build/libs/foxy-eckoes-0.0.1-SNAPSHOT.jar app.jar

#documents port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

