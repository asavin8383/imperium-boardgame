FROM openjdk:8-jdk

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

ENV APP_PATH="/as-15-8-executor.jar" \
    LOG_PATH="/log" \
    PROFILE="test"

VOLUME $LOG_PATH

COPY /target/as-15-8-executor.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-executor.jar"]