FROM openjdk:17-oracle as builder

COPY --chown=gradle:gradle . /app
WORKDIR /app

RUN microdnf install findutils
RUN ./gradlew build

FROM openjdk:17-oracle as runner

WORKDIR /app

COPY --from=builder /app/build/libs/project-mnt-server-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]
