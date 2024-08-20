FROM openjdk:22-jdk-slim
VOLUME /tmp
COPY target/performrev-0.0.1-SNAPSHOT.jar performrev-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/performrev-0.0.1-SNAPSHOT.jar"]