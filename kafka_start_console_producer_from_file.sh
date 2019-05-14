#!/usr/bin/env bash


log_file_name="$PWD/logs/app.log"
read -e -p "Enter log file name: " -i "$log_file_name" log_file_name

bootstrap_server="localhost:9092"
read -e -p "Enter the zk host/port: " -i "$bootstrap_server" bootstrap_server
topic_name="logs"
read -e -p "Enter the topic name: " -i "$topic_name" topic_name

cmd="kafka-console-producer.sh --broker-list $bootstrap_server  --topic $topic_name< $log_file_name"
echo "$cmd"
eval "$cmd"
