FROM adoptopenjdk/openjdk11
RUN mkdir -p app/data
COPY swancloud-application/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]