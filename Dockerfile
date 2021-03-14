FROM gradle:6.7.1-jdk11-openj9 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon && java -Djarmode=layertools -jar build/libs/SwanCloudServer-0.0.1-SNAPSHOT.jar extract

FROM adoptopenjdk/openjdk11
RUN mkdir -p app/data
WORKDIR app
ARG EXPLODED_PATH=home/gradle/src
COPY --from=build $EXPLODED_PATH/dependencies/ /app/
COPY --from=build $EXPLODED_PATH/snapshot-dependencies/ /app/
COPY --from=build $EXPLODED_PATH/spring-boot-loader/ /app/
COPY --from=build $EXPLODED_PATH/application/ /app/
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]