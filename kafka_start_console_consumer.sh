#!/usr/bin/env bash

kafka-proxied/kafka/start_kafka_console_consumer.sh 
# $KAFKA_HOME/bin/kafka-console-consumer.sh --new-consumer --bootstrap-server localhost:9092 --topic logs  --from-beginning --delete-consumer-offsets
