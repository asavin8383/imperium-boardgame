FROM openjdk:8-jdk-alpine

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15812
ENV PROFILE test
ENV APP_PATH="/as-15-8-rep-scheduler.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/config"

VOLUME $LOG_PATH
VOLUME $CONF_PATH

COPY /target/as-15-8-rep-scheduler.jar  $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=/log", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-rep-scheduler.jar"]