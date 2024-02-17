FROM openjdk:17-jdk-alpine
COPY ./target/*jar app.jar

ENV DB_HOST=localhost
ENV EUREKA_HOST=localhost

ENTRYPOINT [ "sh", "-c", "java \
    -jar /app.jar \
    --cloud.db-host=$DB_HOST \
    --cloud.eureka-host=$EUREKA_HOST \
    " ]