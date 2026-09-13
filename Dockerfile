# Build CareerVerse with Java 17 and run it on Tomcat 10.1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM tomcat:10.1.59-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/*
COPY --from=build /build/target/careerverse.war /usr/local/tomcat/webapps/careerverse.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
