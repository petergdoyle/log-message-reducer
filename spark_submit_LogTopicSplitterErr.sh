#!/usr/bin/env bash

class_name_splitter_err='com.cleverfishsoftware.challenge.scala.LogTopicSplitterErr'

spark_cluster_address='spark://log-message-reducer.cleverfishsoftware.com:7077'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet='--driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"'
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

broker_list='localhost:9092'
consumer_group_id_err="LogTopicSplitterErr-cg"
consumer_topic_name="logs"
producer_err_topic_name="logs-stderr"
checkpoint_dir="/spark/checkpoint"

    # val brokers=args(0)
    # val consumerGroupId=args(1)
    # val consumerTopic=args(2)
    # val producerErrTopic=args(3)
    # val checkpointDir=args(4)

err_params="$broker_list $consumer_group_id_err $consumer_topic_name $producer_err_topic_name $checkpoint_dir"


skip_build=false
for var in "$@"
do
    echo "$var"
    if  [ "$var" == "--skipBuild" ]; then
      skip_build=true
    fi
done

build_status=0
if  ! $skip_build
then
  mvn -f log-message-reducer/pom.xml package
  build_status=$?
fi

if test $build_status -ne 0; then
  exit 1
fi

cmd="spark-submit $driver_java_options --master $spark_cluster_address --jars $uber_jar --class $class_name_splitter_err $jar $err_params"
echo "$cmd"
eval "$cmd"
