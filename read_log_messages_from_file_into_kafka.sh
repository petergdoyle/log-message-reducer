#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.KafkaMessageSenderFromFile'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'


log_file_name="$PWD/logs/app.log"
read -e -p "Enter log file name: " -i "$log_file_name" log_file_name

mvn -f log-message-generator/pom.xml package

cmd="java -cp $jar_name $class_name $log_file_name"
echo "$cmd"
eval "$cmd"
