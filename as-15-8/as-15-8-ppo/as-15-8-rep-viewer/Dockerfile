FROM tomcat:8-alpine

ENV BIRT_VIEWER_WORKING_FOLDER /var/opt/reports
ENV WEBAPPS_PATH $CATALINA_HOME/webapps
ENV BIRT_APP birt
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

#Download Birt Runtime
    COPY "birt-runtime/WebViewerExample/" "$WEBAPPS_PATH/$BIRT_APP"

#Install MSSQL, PostgreSQL and MySql drivers
RUN ls "$WEBAPPS_PATH/$BIRT_APP" && \
    wget "https://download.microsoft.com/download/0/2/A/02AAE597-3865-456C-AE7F-613F99F850A8/sqljdbc_6.0.8112.200_enu.tar.gz" -P /tmp && \
    wget "https://jdbc.postgresql.org/download/postgresql-42.2.2.jar" -P "$WEBAPPS_PATH/$BIRT_APP/WEB-INF/lib/" && \
    wget "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.46.tar.gz" -P /tmp && \
    tar -xf "/tmp/sqljdbc_6.0.8112.200_enu.tar.gz" -C "$WEBAPPS_PATH/$BIRT_APP/WEB-INF/lib/" --strip-components=3 sqljdbc_6.0/enu/jre8/sqljdbc42.jar && \
    tar -xf "/tmp/mysql-connector-java-5.1.46.tar.gz" -C "$WEBAPPS_PATH/$BIRT_APP/WEB-INF/lib/" --strip-components=1 mysql-connector-java-5.1.46/mysql-connector-java-5.1.46-bin.jar && \
    rm -rf /tmp/*
COPY *.rptdesign "$WEBAPPS_PATH/$BIRT_APP/"
# #Create the reports folder
# RUN mkdir "$BIRT_VIEWER_WORKING_FOLDER" && \
#     mkdir "$BIRT_VIEWER_WORKING_FOLDER/samples"

# COPY ./templates/reports/*.* "$BIRT_VIEWER_WORKING_FOLDER/samples/"

# RUN mkdir "$CATALINA_HOME/conf/Catalina" && \
#     mkdir "$CATALINA_HOME/conf/Catalina/localhost/" 

# COPY ./templates/run.sh /usr/local/bin/run.sh
# COPY ./templates/context.xml "  "

# RUN chmod +x /usr/local/bin/run.sh

# RUN rm -rf "${CATALINA_HOME}/webapps/ROOT"

# #Modify the web.xml to change the reports folder
# COPY ./templates/web.xml "$WEBAPPS_PATH/$BIRT_APP/WEB-INF/web.xml"

# #Modify the landing page
# COPY ./templates/website/ "$WEBAPPS_PATH/$BIRT_APP"

# #Map the birt reports folder
# VOLUME $BIRT_VIEWER_WORKING_FOLDER

# CMD ["sh", "-c", "/usr/local/bin/run.sh ${CATALINA_HOME} ${CONTEXT_PATH}" ]