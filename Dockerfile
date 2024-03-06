FROM openjdk:17-jdk-alpine
COPY target/storage_service-1.0-SNAPSHOT.jar storage_service-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/storage_service-1.0-SNAPSHOT.jar"]
