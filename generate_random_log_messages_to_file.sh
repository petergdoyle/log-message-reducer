#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagebuillder.RunLogMessageBuilderToFile'
jar_name='log-message-builder/target/log-message-builder-1.0-SNAPSHOT.jar'
records_to_write='10000'
log_file_name="$PWD/logs/log-message-builder.log" #this needs to map to what is in the log-4j appender config

rm -frv $log_file_name
mvn -f log-message-builder/pom.xml clean install \
  && java -Duser.timezone=UTC -cp $jar_name $class_name $records_to_write \
  && echo "total: $(wc -l <$log_file_name)" \
  && echo "errors: $(grep -c 'error' $log_file_name)" \
  && echo "warn: $(grep -c 'warn' $log_file_name)" \
  && echo "info: $(grep -c 'info' $log_file_name)" \
  && echo "debug: $(grep -c 'debug' $log_file_name)" \
  && echo "trace: $(grep -c 'trace' $log_file_name)"
