FROM openjdk:17-jdk-alpine
COPY ./target/*jar app.jar

ENV DB_HOST=localhost
ENV EUREKA_HOST=localhost
ENV KAFKA_HOST=localhost
#ENV CURRENCY_URL="http://localhost:8080"

EXPOSE 8090

ENTRYPOINT [ "sh", "-c", "java \
    -jar /app.jar \
    --cloud.db-host=$DB_HOST --cloud.kafka-host=$KAFKA_HOST \
    --cloud.eureka-host=$EUREKA_HOST \
    " ]