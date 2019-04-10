#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagebuillder.RunKafkaMessageSender'
jar_name='log-message-builder/target/log-message-builder-1.0-SNAPSHOT.jar'
log_file_name="$PWD/logs/log-message-builder.log"

# mvn -f log-message-builder/pom.xml clean install \
# && \
java -cp $jar_name $class_name $log_file_name
