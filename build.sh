#!/bin/sh

export JAVA_HOME="/usr/lib64/java/jdk-17.0.1"

echo "Building jar..."

mvn clean package

echo "Starting docker..."

systemctl start docker

echo "Docker started."

echo "Build storage-api docker image..."

docker build . -t ickee953/storage-api:1.0.0

echo "Done."

echo "To run StorageService type on terminal: 'docker run -e SERVER_STORAGE_PORT=8081 ickee953/storage-api:1.0.'"
