FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN mvn clean package

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

FROM eclipse-temurin:21-jre

COPY --from=build /workspace/app/target/viisp-auth-helper-*.jar viisp-auth-helper.jar

EXPOSE 8080

CMD ["java", "-jar", "viisp-auth-helper.jar"]
