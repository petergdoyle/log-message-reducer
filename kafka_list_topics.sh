#!/usr/bin/env bash

if [ -z $KAFKA_HOME ]; then
  display_error "No env var KAFKA_HOME is set. Source your ~/.bash_profile or logout and log back in"
  exit 1
fi

zk_host_port="localhost:2181"
read -e -p "Enter the zk host/port: " -i "$zk_host_port" zk_host_port

cmd="kafka-topics.sh --list --zookeeper $zk_host_port"
echo "$cmd"
eval "$cmd"
