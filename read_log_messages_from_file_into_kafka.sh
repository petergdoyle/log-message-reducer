#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.RunKafkaMessageSenderFromFile'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'
log_file_name="$PWD/logs/log-message-generator.log"

# mvn -f log-message-generator/pom.xml clean install \
# && \
java -cp $jar_name $class_name $log_file_name
