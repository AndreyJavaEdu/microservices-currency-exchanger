FROM openjdk:17-jdk-alpine
COPY ./target/*jar app.jar
EXPOSE 8761
WORKDIR /
ENTRYPOINT ["java", "-jar", "/app.jar"]
