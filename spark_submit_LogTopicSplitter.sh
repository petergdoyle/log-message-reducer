#!/usr/bin/env bash

class_name='com.cleverfishsoftware.challenge.scala.LogTopicSplitter'
spark_cluster_address='spark://log-message-reducer.cleverfishsoftware.com:7077'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet='--driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"'
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

# System.err.println("Usage: LogTopicSplitter <brokers> <consumerGroupId> <consumer-topic> <producerErrTopic> <producerOutTopic> <checkpointsDir> <outputsDir>\n"
#         + "  <brokers> is a list of one or more Kafka brokers\n"
#         + "  <consumerGroupId> is a consumer group name to consume from topics\n"
#         + "  <consumerTopic> the topic to listen for mixed log messages\n"
#         + "  <producerErrTopic> the topic to put error messages onto \n"
#         + "  <producerOutTopic> the topic to put non-error messages onto \n"
#         + "  <checkpointDir> the location for spark streaming checkpoints\n"
#         + "  <outputsDir> the location for any console output\n"

broker_list='localhost:9092'
consumer_topic_name="logs"
consumer_group_id="LogTopicSplitter-cg"
producer_err_topic_name="logs-stderr"
producer_out_topic_name="logs-stdout"
checkpoint_dir="/spark/checkpoint"
violations_dir="$PWD/violations"
params="$broker_list $consumer_group_id $consumer_topic_name $producer_err_topic_name $producer_out_topic_name $checkpoint_dir $violations_dir"
skip_build="$1"
status=0
if  [ "$skip_build" != "--skipBuild" ]; then
  mvn -f log-message-reducer/pom.xml package
  status=$?
fi

if test $status -eq 0; then
  spark-submit $driver_java_options --master $spark_cluster_address --jars $uber_jar --class $class_name $jar $params
fi
