FROM ubuntu:bionic AS builder
MAINTAINER eplauchu@immortalcrab.com

RUN apt-get update

RUN apt-get install -y openjdk-11-jdk default-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME="/usr/lib/jvm/default-java"

COPY service /service
WORKDIR /service/workclock

RUN mvn install

WORKDIR /service/server

RUN mvn package

FROM adoptopenjdk/openjdk11:alpine

# Copy the jar to the production image from the builder stage.
COPY --from=builder /service/server/target/web-server-LOLA-TRAILERA-fat.jar srv.jar

EXPOSE 4567

# Run the web service on container startup.
CMD ["java", "-cp", "srv.jar", "com.immortalcrab.cfdi.server.Launcher"]
