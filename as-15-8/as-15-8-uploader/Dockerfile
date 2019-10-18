FROM openjdk:8-jdk-alpine

LABEL Alexander Savin <asavin@ec-leasing.ru>

EXPOSE 15801

ENV APP_PATH="/as-15-8-uploader.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/config" \
    PROFILE="test"

COPY target/as-15-8-uploader.jar $APP_PATH

VOLUME $LOG_PATH
VOLUME $CONF_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-uploader.jar"]