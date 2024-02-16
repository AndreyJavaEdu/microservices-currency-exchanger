FROM openjdk:17-jdk-alpine
COPY ./target/*jar app.jar

ENV EUREKA_HOST=localhost
ENV PROCESSING_URL=http://localhost:8090
ENV CURRENCY_URL=http://localhost:8084
ENV AUTH_URL=http://localhost:9797
ENV HISTORY_URL=http://localhost:8015

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java -jar /app.jar \
    --cloud.eureka-host=$EUREKA_HOST \
    --cloud.processing-service-url=$PROCESSING_URL \
    --cloud.currency-service-url=$CURRENCY_URL \
    --cloud.account-history-service-url=$HISTORY_URL \
    --cloud.identity-service-url=$AUTH_URL \
    " ]