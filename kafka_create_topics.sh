#!/usr/bin/env bash

if [ -z $KAFKA_HOME ]; then
  display_error "No env var KAFKA_HOME is set. Install Kafka or source your ~/.bash_profile or logout and log back in"
  exit 1
fi

zk_host_port="localhost:2181"
read -e -p "Enter the zk host/port: " -i "$zk_host_port" zk_host_port

kafka-topics.sh --create --zookeeper $zk_host_port --replication-factor 1 --partitions 2 --topic logs --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
kafka-topics.sh --create --zookeeper $zk_host_port --replication-factor 1 --partitions 2 --topic logs-stderr --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
kafka-topics.sh --create --zookeeper $zk_host_port --replication-factor 1 --partitions 2 --topic logs-stdout --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
kafka-topics.sh --create --zookeeper $zk_host_port --replication-factor 1 --partitions 2 --topic logs-reduced --config max.message.bytes=262144 --config retention.bytes=26214400 --config retention.ms=3600000
