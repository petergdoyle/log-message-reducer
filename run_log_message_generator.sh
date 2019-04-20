#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.RunLogMessageGenerator'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'

log4j_properties="-Dlog4j.configuration=file:/path/to/log4j.properties"
total_messages='1000'
message_rate='20.0'
error_rate='0.05'
params="$total_messages $message_rate $error_rate"

log_file_name="$PWD/logs/application.log" #this needs to map to what is in the log-4j appender config
if [ -f $log_file_name ]; then
  prompt="The generated log file already exists. Do you want to delete it? (y/n): "
  default_value="y"
  read -e -p "$(echo -e $prompt)" -i $default_value response
  if [ "$response" == 'y' ]; then
    sudo rm -fv $log_file_name/*
  fi
fi

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
  mvn -f log-message-generator/pom.xml package
  build_status=$?
fi

if test $build_status -ne 0; then
  exit 1
fi

time java -Duser.timezone=UTC -cp $jar_name $class_name $params \
  && echo "total: $(wc -l <$log_file_name)" \
  && echo "errors: $(grep -c 'error' $log_file_name)" \
  && echo "warn: $(grep -c 'warn' $log_file_name)" \
  && echo "info: $(grep -c 'info' $log_file_name)" \
  && echo "debug: $(grep -c 'debug' $log_file_name)" \
  && echo "trace: $(grep -c 'trace' $log_file_name)"
