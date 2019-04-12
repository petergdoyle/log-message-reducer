#!/usr/bin/env bash
echo "stopping worker..."
sleep 1
$SPARK_HOME/sbin/stop-slave.sh
