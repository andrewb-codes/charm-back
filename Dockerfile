FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY back/pom.xml back/pom.xml
COPY linecount-maven-plugin/pom.xml linecount-maven-plugin/pom.xml
COPY back/src back/src
COPY linecount-maven-plugin/src linecount-maven-plugin/src

RUN mvn -pl back -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/back/target/back-1.0-SNAPSHOT.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.war"]
