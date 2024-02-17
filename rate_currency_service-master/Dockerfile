FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ./${JAR_FILE} app.jar
WORKDIR /
ENV EUREKA_HOST=localhost
ENV JAVA_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8787,suspend=n"
EXPOSE 8084 8787

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Duser.country=RU -Duser.language=ru \
    -jar /app.jar \
    --cloud.eureka-host=$EUREKA_HOST \
    " ]