FROM gradle:jdk21 AS build

WORKDIR /auth
COPY . .
RUN gradle build --stacktrace --info

FROM eclipse-temurin:21-jre

WORKDIR /auth
COPY --from=build /auth/build/libs/*.jar auth.jar

ENTRYPOINT ["java", "-jar", "auth.jar"]
