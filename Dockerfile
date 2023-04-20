FROM maven:3.6.0-jdk-11-slim
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
EXPOSE 8080
WORKDIR /home/app
CMD ["mvn", "spring-boot:run"]

