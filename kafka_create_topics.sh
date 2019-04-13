#!/usr/bin/env bash
# kafka-proxied/kafka/create_topic.sh

$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic logs --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic logs-stderr --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic logs-stdout --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
