FROM openjdk:8-jdk

LABEL Alex Shabalin <ashabalin@ec-leasing.ru>

EXPOSE 11300

ENV APP_PATH="/as-15-8-dispatcher.jar" \
    LOG_PATH="/log" \
    KAFKA_STREAMS_PATH="/var/lib/kafka-streams"
    KAFKA_STREAMS_TMP_PATH="/tmp/kafka-streams"

VOLUME $LOG_PATH

VOLUME $KAFKA_STREAMS_PATH

VOLUME $KAFKA_STREAMS_TMP_PATH

COPY /target/as-15-8-dispatcher.jar $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=${LOG_PATH}", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms4G", \
         "-Xmx23G", \
         "/as-15-8-dispatcher.jar"]