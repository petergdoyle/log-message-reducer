#!/usr/bin/env bash

class_name='com.cleverfishsoftware.challenge.scala.LogMessageReducer'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet='--driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"'
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

broker_list='engine2:9092'
consumer_group_id="LogMessageReducer-cg"
consumer_topic_std_out="logs-stdout"
consumer_topic_std_err="logs-stderr"
producer_topic_reduced="logs-reduced"
checkpoint_dir="/spark/checkpoint"

# val brokers=args(0)
# val consumerGroupId=args(1)
# val consumerTopicStdOut=args(2)
# val consumerTopicStdErr=args(3)
# val producerReducedTopic=args(4)
# val checkpointDir=args(5)

params="$broker_list $consumer_group_id $consumer_topic_std_out $consumer_topic_std_err $producer_topic_reduced $checkpoint_dir"

skip_build=false
for var in "$@"
do
    echo "$var"
    if  [ "$var" != "--skipBuild" ]; then
      skip_build=true
    fi
done

build_status=0
if  ! $skip_build
then
  mvn --offline -f log-message-reducer/pom.xml package
  build_status=$?
fi

spark_cluster_address='spark://log-message-reducer.cleverfishsoftware.com:7077'
mode_local=""
mode_cluster="--deploy-mode cluster"
deploy_mode=$mode_local

if test $build_status -eq 0; then
  cmd="spark-submit $driver_java_options --master $deploy_mode $spark_cluster_address --jars $uber_jar --class $class_name $jar $params"
  echo "$cmd"
fi
