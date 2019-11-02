FROM openjdk:8-jdk-alpine

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15813

ENV APP_PATH="/as-15-8-birt.jar" \
    LOG_PATH="/log" \
    PROFILE="dev_asin"

VOLUME $LOG_PATH

COPY /target/as-15-8-birt.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "${APP_PATH}"]