#!/usr/bin/env bash

class_name='com.cleverfishsoftware.challenge.scala.LogStats'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet='--driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"'
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

# val brokers=args(0)
# val consumerGroupId=args(1)
# val topic=args(2)
# val checkpointDir=args(3)

broker_list="localhost:9092"
read -e -p "[LogMessageReducer] Enter the Kafka Broker list: " -i "$broker_list" broker_list
consumer_group_id="LogStats-cg"
read -e -p "[LogMessageReducer] Enter the Kafka Consumer Group name: " -i "$consumer_group_id" consumer_group_id
topic="logs"
read -e -p "[LogMessageReducer] Enter the topic name log messages: " -i "$topic" topic

checkpoint_dir="/tmp/spark/checkpoint"
params="$broker_list $consumer_group_id $topic $checkpoint_dir"

skip_build="$1"
status=0
if  [ "$skip_build" != "--skipBuild" ]; then
  mvn -f log-message-reducer/pom.xml package
  status=$?
fi

if test $status -ne 0; then
  exit 1
fi

spark_cluster_master_name=$(cat /usr/spark/default/conf/spark-cluster.info |grep MASTER_NODE_NAME| cut -d "=" -f 2)
if [ $spark_cluster_master_name == "" ]; then
  spark_cluster_master_name="localhost"
fi
spark_cluster_master_address="spark://$spark_cluster_master_name:7077"
read -e -p "[LogMessageReducer] Enter spark url for the Spark Master Node: " -i "$spark_cluster_master_address" spark_cluster_master_address

mode_cluster="--deploy-mode cluster"
deploy_mode=""
deploy_mode_value="local"
read -e -p "[LogMessageReducer] Enter spark deployment mode (local/cluster) to run the Driver Program: " -i "$deploy_mode_value" deploy_mode_value
if [ "deploy_mode_value" == "cluster" ]; then
  deploy_mode="mode_cluster"
fi

cmd="spark-submit $driver_java_options $deploy_mode --master $spark_cluster_master_address --jars $uber_jar --class $class_name $jar $params"
echo -e "The following command will be run:\n$cmd"
read -n 1 -s -r -p "Press any key to continue"
eval "time $cmd"
