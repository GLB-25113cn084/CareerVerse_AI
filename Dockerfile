# Build CareerVerse with Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM tomcat:10.1.59-jdk17-temurin

ENV CATALINA_BASE=/tmp/tomcat

RUN mkdir -p /tmp/tomcat \
    && cp -a /usr/local/tomcat/conf /tmp/tomcat/ \
    && mkdir -p /tmp/tomcat/logs \
    && mkdir -p /tmp/tomcat/temp \
    && mkdir -p /tmp/tomcat/work \
    && mkdir -p /tmp/tomcat/webapps

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /build/target/careerverse.war /tmp/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
