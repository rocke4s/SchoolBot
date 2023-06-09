FROM openjdk:latest
WORKDIR /usr/src/app
COPY target/AgBotMax-1.0-SNAPSHOT-jar-with-dependencies.jar .
COPY src/main/resources/application.properties .
COPY botSchedule.db .
EXPOSE 80
CMD ["java", "-jar", "AgBotMax-1.0-SNAPSHOT-jar-with-dependencies.jar"]