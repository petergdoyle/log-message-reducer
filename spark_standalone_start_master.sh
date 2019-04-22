#!/usr/bin/env bash


echo "starting master..."
sleep 1
$SPARK_HOME/sbin/start-master.sh

log_file="$SPARK_HOME/logs/spark-vagrant-org.apache.spark.deploy.master.Master-1-log-message-reducer.cleverfishsoftware.com.out"
echo "tailing on master log for 5s..."
sleep 1
timeout 5s tail -f $log_file
