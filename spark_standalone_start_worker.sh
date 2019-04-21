#!/usr/bin/env bash

echo "starting worker..."
sleep 1
$SPARK_HOME/sbin/start-slave.sh spark://log-message-reducer.cleverfishsoftware.com:7077

echo "tailing on worker log for 5s..."
sleep 1
timeout 5s tail -f $log_file
