#!/usr/bin/env bash

class_name='com.cleverfishsoftware.challenge.scala.LogTopicSplitter'
spark_cluster_address='spark://log-message-reducer.cleverfishsoftware.com:7077'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet='--driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"'
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

broker_list='localhost:9092'
consumer_topic_name="logs"
consumer_group_id="LogTopicSplitter-cg"
producer_err_topic_name="logs-stderr"
producer_out_topic_name="logs-stdout"
checkpoint_dir="/spark/checkpoint"

    # val brokers=args(0)
    # val consumerGroupId=args(1)
    # val consumerTopic=args(2)
    # val producerErrTopic=args(3)
    # val producerOutTopic=args(4)
    # val checkpointDir=args(5)

params="$broker_list $consumer_group_id $consumer_topic_name $producer_err_topic_name $producer_out_topic_name $checkpoint_dir"

skip_build="$1"
status=0
if  [ "$skip_build" != "--skipBuild" ]; then
  mvn --offline -f log-message-reducer/pom.xml package
  status=$?
fi

if test $status -eq 0; then
  spark-submit $driver_java_options --master $spark_cluster_address --jars $uber_jar --class $class_name $jar $params
fi
