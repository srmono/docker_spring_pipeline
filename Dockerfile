#use open jdk as the base image
FROM openjdk:17-jdk-alpine

# set working directory
WORKDIR /app

#copy the jar file to the container working dire
COPY target/fms-0.0.1-SNAPSHOT.jar  app.jar

#Expose tje a[[;opplication port
EXPOSE 8081

CMD ["java", "-jar", "app.jar"]