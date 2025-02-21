FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl netcat-openbsd
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
