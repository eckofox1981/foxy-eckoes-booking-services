FROM eclipse-temurin:25-jre-alpine
LABEL authors="eckofox"

WORKDIR /app

COPY /build/libs/foxy-eckoes-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

