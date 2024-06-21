FROM openjdk:14-ea-5-jdk-oracle
WORKDIR /app
COPY gradle ./gradle
COPY gradle.properties gradlew settings.gradle system.properties build.gradle application.yml Procfile ./
COPY application-local-docker.yml ./application-local.yml
COPY src ./src
RUN echo "bootRun {jvmArgs(['-agentlib:jdwp=transport=dt_socket,server=y,address=*:8005,suspend=n'])}" >> build.gradle
CMD ./gradlew bootRun
EXPOSE 8005