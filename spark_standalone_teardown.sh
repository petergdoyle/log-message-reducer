#!/usr/bin/env bash

./spark_standalone_stop_worker.sh
echo "removing old worker log file..."
sleep 1
log_file="$SPARK_HOME/logs/spark-vagrant-org.apache.spark.deploy.worker.Worker-1-log-message-reducer.cleverfishsoftware.com.out"
rm -v $log_file

./spark_standalone_stop_master.sh
echo "removing old master log file..."
sleep 1
log_file="$SPARK_HOME/logs/spark-vagrant-org.apache.spark.deploy.master.Master-1-log-message-reducer.cleverfishsoftware.com.out"
rm -v $log_file


checkpoint_dir='/spark/checkpoint'
if [ ! "$(ls -A $checkpoint_dir)" ]; then
  echo "The Spark checkpoint directory is empty";
else
  prompt="The Spark checkpoint directory is not empty. Do you want to delete it? (y/n): "
  default_value="y"
  read -e -p "$(echo -e $prompt)" -i $default_value response
  if [ "$response" == 'y' ]; then
    sudo rm -frv $checkpoint_dir/*
  fi
fi
