#!/usr/bin/env bash

class_name='com.cleverfishsoftware.challenge.scala.LogMessageReducer'
jar="$PWD/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar"
uber_jar="$PWD/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar"
driver_java_options_quiet="--driver-java-options \"-Dlog4j.configuration=file://$PWD/spark_log4j_QUIET.properties\""
driver_java_options_default=''
driver_java_options=$driver_java_options_quiet

spark_checkpoint_dir="/tmp/spark/checkpoint"
if test -d $SPARK_HOME/conf/spark-cluster.info; then
  spark_checkpoint_dir="$(cat /usr/spark/default/conf/spark-cluster.info |grep SPARK_CHECKPOINT_DIR| cut -d "=" -f 2)"
fi
#
# if test -d $spark_checkpoint_dir; then
#  # "exists"
#   if test "$(ls -A $spark_checkpoint_dir)"; then
#     # "not empty"
#     prompt="The Spark checkpoint directory $spark_checkpoint_dir is not empty. Do you want to delete it? (y/n): "
#     default_value="y"
#     read -e -p "$(echo -e $prompt)" -i $default_value response
#     if [ "$response" == 'y' ]; then
#       sudo rm -frv $spark_checkpoint_dir/*
#     fi
#   fi
# else
#   # "not exists "
# fi

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

if test $build_status -ne 0; then
  exit 1
fi

broker_list="localhost:9092" # this has to match what is in the log4j2 file
read -e -p "[LogMessageReducer] Enter the Kafka Broker list: " -i "$broker_list" broker_list
consumer_group_id="LogMessageReducer-cg"
read -e -p "[LogMessageReducer] Enter the Kafka Consumer Group name: " -i "$consumer_group_id" consumer_group_id
consumer_topic_std_out="logs-stdout"
read -e -p "[LogMessageReducer] Enter the topic name for STDOUT messages: " -i "$consumer_topic_std_out" consumer_topic_std_out
consumer_topic_std_err="logs-stderr"
read -e -p "[LogMessageReducer] Enter the topic name for STDERR messages: " -i "$consumer_topic_std_err" consumer_topic_std_err
producer_topic_reduced="logs-reduced"
read -e -p "[LogMessageReducer] Enter the topic name for the Joined messages: " -i "$producer_topic_reduced" producer_topic_reduced
checkpoint_dir="/tmp/spark/checkpoint"
params="$broker_list $consumer_group_id $consumer_topic_std_out $consumer_topic_std_err $producer_topic_reduced $checkpoint_dir"

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

cmd="spark-submit $driver_java_options $deploy_mode --master $spark_cluster_master_address --supervise  --jars $uber_jar --class $class_name $jar $params"
echo -e "The following command will be run:\n$cmd"
read -n 1 -s -r -p "Press any key to continue"
eval "time $cmd"
