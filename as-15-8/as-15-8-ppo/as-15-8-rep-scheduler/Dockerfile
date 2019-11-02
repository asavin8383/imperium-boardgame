FROM openjdk:8-jdk-alpine

LABEL Kirill Lychagin <klychagin@ec-leasing.ru>

EXPOSE 15812

VOLUME /log

COPY /target/as-15-8-rep-scheduler.jar /as-15-8-rep-scheduler.jar

CMD ["java", \
         "-jar", \
         "-Dapplication.logpath=/log", \
         "-Dspring.profiles.active=dev_asin", \
         "-Xms512M", \
         "-Xmx1024M", \
         "/as-15-8-rep-scheduler.jar"]