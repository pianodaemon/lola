FROM ubuntu:bionic AS cfdi-workclock
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

CMD ["mvn", "exec:java"]
