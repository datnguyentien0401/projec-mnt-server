FROM openjdk:17-oracle

COPY ./build/libs/projec-mnt-server-0.0.1-SNAPSHOT.jar /app/

WORKDIR /app
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "projec-mnt-server-0.0.1-SNAPSHOT.jar"]
