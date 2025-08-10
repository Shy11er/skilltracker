FROM gradle:8.6-jdk21 AS build

WORKDIR /app
COPY . .
RUN gradle clean --no-daemon --refresh-dependencies
RUN gradle :auth:build --stacktrace --info

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/auth/build/libs/*.jar auth.jar
ENTRYPOINT ["java", "-jar", "auth.jar", "--spring.profiles.active=prod"]
