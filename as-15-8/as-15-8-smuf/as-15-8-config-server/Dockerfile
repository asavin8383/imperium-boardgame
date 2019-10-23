FROM openjdk:8-jdk-alpine

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

EXPOSE 15802

ENV APP_PATH="/as-15-8-config-server.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/config"

COPY target/as-15-8-config-server.jar $APP_PATH

VOLUME $LOG_PATH
VOLUME $CONF_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-config-server.jar"]