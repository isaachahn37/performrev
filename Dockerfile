FROM openjdk:22-jdk-slim
VOLUME /tmp
COPY target/performrev-0.0.1-SNAPSHOT.jar performrev-0.0.1-SNAPSHOT.jar
EXPOSE 8080
EXPOSE 27017
EXPOSE 9092
ENTRYPOINT ["java","-jar","/performrev-0.0.1-SNAPSHOT.jar"]
