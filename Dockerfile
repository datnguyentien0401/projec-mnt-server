FROM openjdk:17-oracle as builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN ./gradlew --no-daemon build

COPY src ./src

RUN ./gradlew build

FROM openjdk:17-oracle as runner

WORKDIR /app

COPY --from=builder /app/build/libs/project-mnt-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "/build/libs/app.jar"]
