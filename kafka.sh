docker run --name kafka -p9092 \
  -e KAFKA_ADVERTISED_HOST_NAME=172.17.0.1 -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.1:2181 \
  -e KAFKA_CREATE_TOPICS="account-events:4:1" \
  -d wurstmeiser/kafka