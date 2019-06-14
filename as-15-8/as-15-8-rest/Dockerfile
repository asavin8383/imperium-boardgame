FROM openjdk:8-jdk-alpine

LABEL Alexander Savin <asavin@ec-leasing.ru>

EXPOSE 15800

ENV APP_PATH="/lib/as-15-8-rest.jar" \
    LOG_PATH="/log" \
    CONFIG_PATH="/config" \
    PROFILE="test"

VOLUME $LOG_PATH \
    $CONFIG_PATH

COPY /target/as-15-8-rest.jar $APP_PATH
COPY /src/main/resources/application.yml $CONFIG_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/lib/as-15-8-rest.jar"]