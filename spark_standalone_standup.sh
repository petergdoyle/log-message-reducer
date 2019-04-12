#!/usr/bin/env bash

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

./spark_standalone_start_master.sh
./spark_standalone_start_worker.sh
