FROM openjdk:8-jdk-alpine

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15813
ENV PROFILE test

ENV APP_PATH="/as-15-8-birt.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/config"

VOLUME $LOG_PATH
VOLUME $CONF_PATH


#Install MS True Type, Ionicons and Font Awesome fonts
RUN rm -rf /var/cache/apk/* && \
    rm -rf /tmp/* && \
    apk add --update msttcorefonts-installer && \
    update-ms-fonts && \
    mkdir /usr/share/fonts/ko && \
    mkdir /usr/share/fonts/ko/TrueType && \
    cp /usr/share/fonts/truetype/msttcorefonts/*.* /usr/share/fonts/ko/TrueType/ && \
    rm -rf /var/cache/apk/* && \
    rm -rf /tmp/*


COPY /target/as-15-8-birt.jar  $APP_PATH

ENTRYPOINT ["java", \
         "-jar", \
#         "-Dapplication.logpath=/log", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-birt.jar"]
