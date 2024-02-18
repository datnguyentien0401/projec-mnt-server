FROM openjdk:17-oracle AS builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN microdnf install findutils
RUN ./gradlew build

FROM openjdk:17-oracle AS runner

WORKDIR /app

COPY --from=builder /home/gradle/src/build/libs/projec-mnt-server-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]
