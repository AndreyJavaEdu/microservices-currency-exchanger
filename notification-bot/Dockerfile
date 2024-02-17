FROM openjdk:17-jdk-alpine
COPY ./build/libs/*jar app.jar
WORKDIR /
ENV TELEGRAM_BOT_TOKEN=.\notification-bot\conf\telegram.token
ENV KAFKA_HOST=localhost
ENV IDENTITY_SERVICE="http://localhost:9797/auth/token"


EXPOSE 8015

ENTRYPOINT [ "sh", "-c", "java \
    -jar /app.jar \
    --telegram.token=$TELEGRAM_BOT_TOKEN \
    --telegram.identity-service=$IDENTITY_SERVICE \
    --cloud.kafka-host=$KAFKA_HOST \
    " ]