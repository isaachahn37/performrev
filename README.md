# performrev


First you have to run mongo and kafka

### Run mongo DB
```
docker run --name mongo -d -p 27017:27017 mongo

```
### Run Zookeper
```
docker run -d --name zookeeper -p 2181:2181 zookeeper

```

### Run Kafka

```
docker run -d --name kafka -p 9092:9092 \
-e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
-e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
--link zookeeper wurstmeister/kafka
```

### Run project locally
```
 mvn spring-boot:run
```

### Run project dockerized
You need to build the docker image first
```
docker build -t performrev .
```
Run your docker image
```
docker run -d   --name performrev   -p 8080:8080   --link kafka   --link mongo   performrev
```