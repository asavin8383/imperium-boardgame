FROM openjdk:8-jdk-alpine

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

EXPOSE 11200

ENV APP_PATH="/as-15-8-analyzer.jar" \
    LOG_PATH="/log" \
    PROFILE="test"

VOLUME $LOG_PATH

COPY /target/as-15-8-analyzer.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-analyzer.jar"]