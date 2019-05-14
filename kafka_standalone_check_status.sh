#!/usr/bin/env bash


ZK_PIDS=`ps ax | grep -i QuorumPeerMain | grep -v grep | awk '{print $1}'`

[[ ! -z $ZK_PIDS ]] \
&& echo -e "Zookeeper process(es) running:\n$ZK_PIDS" \
|| echo -e "Zookeeper process(es) running: No Zookeeper processes running"


BKR_PIDS=`ps ax | grep -i 'kafka\.Kafka' | grep -v grep | awk '{print $1}'`

[[ ! -z $BKR_PIDS ]] \
&& echo -e "Kafka Broker process(es) running:\n$BKR_PIDS" \
|| echo -e "Kafka Broker process(es) running: No Kafka Broker processes running"
