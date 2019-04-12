#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.RunLogMessageGenerator'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'
total_messages='10000'
message_rate='200.0'
log_file_name="$PWD/logs/application.log" #this needs to map to what is in the log-4j appender config
log4j_properties="-Dlog4j.configuration=file:/path/to/log4j.properties"
rm -frv $log_file_name
mvn -f log-message-generator/pom.xml clean install \
  && java -Duser.timezone=UTC -cp $jar_name $class_name $total_messages $message_rate \
  && echo "total: $(wc -l <$log_file_name)" \
  && echo "errors: $(grep -c 'error' $log_file_name)" \
  && echo "warn: $(grep -c 'warn' $log_file_name)" \
  && echo "info: $(grep -c 'info' $log_file_name)" \
  && echo "debug: $(grep -c 'debug' $log_file_name)" \
  && echo "trace: $(grep -c 'trace' $log_file_name)"
