#!/usr/bin/env bash

echo "removing old worker log file..."
sleep 1
log_file="$SPARK_HOME/logs/spark-vagrant-org.apache.spark.deploy.worker.Worker-1-log-message-reducer.cleverfishsoftware.com.out"
rm -v $log_file
sleep 1

echo "starting worker..."
sleep 1
$SPARK_HOME/sbin/start-slave.sh spark://log-message-reducer.cleverfishsoftware.com:7077

echo "tailing on worker log for 5s..."
sleep 1
timeout 5s tail -f $log_file
