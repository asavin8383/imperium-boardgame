FROM openjdk:8-jdk-alpine

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15812

ENV APP_PATH="/as-15-8-rep-scheduler.jar" \
    LOG_PATH="/log" \
    PROFILE="dev_asin"

VOLUME $LOG_PATH

COPY /target/as-15-8-rep-scheduler.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "${APP_PATH}"]