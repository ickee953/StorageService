FROM openjdk:17
COPY target/storage_service-1.0-SNAPSHOT.jar /usr/src/storage_service-1.0-SNAPSHOT.jar
RUN mkdir /usr/src/uploads
WORKDIR /usr/src
ENTRYPOINT ["java","-jar","/usr/src/storage_service-1.0-SNAPSHOT.jar"]
