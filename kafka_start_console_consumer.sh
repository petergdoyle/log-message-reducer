#!/usr/bin/env bash

bootstrap_server="localhost:9092"
read -e -p "Enter the zk host/port: " -i "$bootstrap_server" bootstrap_server
topic_name="logs"
read -e -p "Enter the topic name: " -i "$topic_name" topic_name

cmd="kafka-console-consumer.sh --bootstrap-server $bootstrap_server --topic $topic_name"
echo "$cmd"
eval "$cmd"
