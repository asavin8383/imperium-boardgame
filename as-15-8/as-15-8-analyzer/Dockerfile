FROM openjdk:8-jdk-alpine

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

ENV APP_PATH "/lib/as-15-8-analyzer.jar"
ENV LOG_PATH "/log"
ENV PROFILE "test"

VOLUME $LOG_PATH

COPY /target/as-15-8-analyzer.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "${APP_PATH}"]