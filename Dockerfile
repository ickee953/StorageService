FROM rsunix/yourkit-openjdk17:latest
COPY target/storage_service-1.0-SNAPSHOT.jar /usr/src/storage_service-1.0-SNAPSHOT.jar
WORKDIR /usr/src
ENTRYPOINT ["java","-jar","/usr/src/storage_service-1.0-SNAPSHOT.jar"]
