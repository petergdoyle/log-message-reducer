#!/usr/bin/env bash

class_name='com.cleverfishsoftware.utils.messagegenerator.LogMessageSplitter'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'

skip_build=false
docker=false
for var in "$@"
do
    echo "$var"
    if  [ "$var" == "--skipBuild" ]; then
      skip_build=true
    fi
    if  [ "$var" == "--docker" ]; then
      docker=true
    fi
done

build_status=0
if  ! $skip_build
then
  mvn -f log-message-generator/pom.xml package
  build_status=$?
fi

if test $build_status -ne 0; then
  exit 1
fi

broker_list="engine1:9092"
read -e -p "[LogMessageSplitter] Enter the Kafka Broker list: " -i "$broker_list" broker_list
logs_topic="logs"
read -e -p "[LogMessageSplitter] Enter the Kafka Logs Topic name to take from : " -i "$logs_topic" logs_topic
logs_stderr_topic="logs-stderr"
read -e -p "[LogMessageSplitter] Enter the Kafka Logs Error Topic name to write to: " -i "$logs_stderr_topic" logs_stderr_topic
logs_stdout_topic="logs-stdout"
read -e -p "[LogMessageSplitter] Enter the Kafka Logs Topic name to write to: " -i "$logs_stdout_topic" logs_stdout_topic

params="$broker_list $logs_topic $logs_stderr_topic $logs_stdout_topic"
cmd="java -cp $jar_name $class_name $params"

if ! $docker
then
  echo -e "The following command will be run:\n$cmd"
  read -n 1 -s -r -p "Press any key to continue"
  eval "time $cmd"
else
  echo "$cmd" > log-message-generator/run_log_message_splitter.cmd
fi
