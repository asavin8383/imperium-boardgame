FROM openjdk:8-jdk-alpine

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

EXPOSE 11300

ENV APP_PATH="/as-15-8-dispatcher.jar" \
    LOG_PATH="/log"

VOLUME $LOG_PATH

COPY /target/as-15-8-dispatcher.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-dispatcher.jar"]