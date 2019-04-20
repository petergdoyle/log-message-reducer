#!/usr/bin/env bash


echo "starting master..."
sleep 1
$SPARK_HOME/sbin/start-master.sh

echo "tailing on master log for 5s..."
sleep 1
timeout 5s tail -f $log_file
