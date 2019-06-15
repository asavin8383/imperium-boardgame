FROM openjdk:8-jdk-alpine

LABEL Alexander Savin <asavin@ec-leasing.ru>

EXPOSE 15800

ENV APP_PATH="/as-15-8-rest.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/conf/" \
    PROFILE="test"

VOLUME $LOG_PATH

COPY target/as-15-8-rest.jar $APP_PATH
COPY config/ $CONF_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-rest.jar"]