FROM maven:3.9-eclipse-temurin-17-alpine
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
EXPOSE 8080
WORKDIR /home/app
CMD ["mvn", "spring-boot:run"]

