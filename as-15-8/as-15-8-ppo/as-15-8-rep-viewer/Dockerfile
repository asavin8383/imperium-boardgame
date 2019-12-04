FROM tomcat:8-alpine

ENV BIRT_VIEWER_WORKING_FOLDER /var/opt/reports
ENV WEBAPPS_PATH $CATALINA_HOME/webapps
ENV CONTEXT_PATH ROOT

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

COPY "target/as-15-8-rep-viewer.war" "$WEBAPPS_PATH/"

