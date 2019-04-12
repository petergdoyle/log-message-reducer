#!/usr/bin/env bash

$KAFKA_HOME/bin/kafka-console-consumer.sh --new-consumer --bootstrap-server localhost:9092 --topic logs  --from-beginning --delete-consumer-offsets
