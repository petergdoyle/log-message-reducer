#!/usr/bin/env bash
echo "stopping master..."
sleep 1
$SPARK_HOME/sbin/stop-master.sh
