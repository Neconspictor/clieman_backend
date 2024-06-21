FROM openjdk:14-ea-5-jdk-oracle
WORKDIR /app
COPY gradle ./gradle
COPY gradle.properties gradlew settings.gradle system.properties build.gradle application.yml Procfile ./
COPY application-local-docker.yml ./application-local.yml
COPY src ./src
CMD ["./gradlew", "bootRun"]