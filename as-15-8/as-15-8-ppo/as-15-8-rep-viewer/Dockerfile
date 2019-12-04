FROM tomcat:8.5.47-jdk8-openjdk

ENV BIRT_VIEWER_WORKING_FOLDER /var/opt/reports
ENV WEBAPPS_PATH $CATALINA_HOME/webapps
ENV CONTEXT_PATH ROOT

#Install MS True Type, Ionicons and Font Awesome fonts
run apt-get update && apt-get install -y cabextract xfonts-utils

COPY ttf-mscorefonts-installer_3.6_all.deb .
RUN  \
     dpkg -i ttf-mscorefonts-installer_3.6_all.deb && \
     fc-cache -fv
ENV LC_ALL ru_RU.UTF-8
ENV LANG ru_RU.UTF-8
ENV LANGUAGE ru_RU.UTF-8
RUN apt-get install -y locales locales-all

COPY "target/as-15-8-rep-viewer.war" "$WEBAPPS_PATH/"

