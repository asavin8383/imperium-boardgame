FROM openjdk:8-jdk

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15814
ENV PROFILE test

ENV APP_PATH="/as-15-8-rep-viewer.jar" \
    LOG_PATH="/log" \
    CONF_PATH="/config"

VOLUME $LOG_PATH
VOLUME $CONF_PATH


#Install MS True Type, Ionicons and Font Awesome fonts
RUN apt-get update && apt-get install -y cabextract xfonts-utils

COPY ttf-mscorefonts-installer_3.6_all.deb .
RUN  \
     dpkg -i ttf-mscorefonts-installer_3.6_all.deb && \
     fc-cache -fv
ENV LC_ALL ru_RU.UTF-8
ENV LANG ru_RU.UTF-8
ENV LANGUAGE ru_RU.UTF-8
RUN apt-get install -y locales locales-all

COPY src/ /src/
COPY /target/as-15-8-rep-viewer.jar  $APP_PATH


ENTRYPOINT ["java", \
         "-jar", \
         "-Dapplication.logpath=/log", \
         "-Dspring.profiles.active=${PROFILE}", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-rep-viewer.jar"]
