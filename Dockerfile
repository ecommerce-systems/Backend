
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline --no-transfer-progress --fail-fast
COPY src ./src
RUN ./mvnw clean package -Dmaven.test.skip=true --no-transfer-progress --fail-fast


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/ecommerce-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
