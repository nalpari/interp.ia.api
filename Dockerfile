FROM gradle:8.12.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --stacktrace --info

FROM openjdk:17-jdk-alpine
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*.jar /app/interp.ia.api.jar
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone
CMD ["java", "-jar", "/app/interp.ia.api.jar"]
