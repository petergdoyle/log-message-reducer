#!/usr/bin/env bash
LogMessageGenerator_class_name='com.cleverfishsoftware.utils.messagegenerator.LogMessageGenerator'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'
log4j_properties="-Dlog4j.configuration=file:/path/to/log4j.properties"

if [ -d logs/ ]; then
  rm -fvr "logs/*"
  sleep 1
fi

skip_build=false
clean=""
docker=false
for var in "$@"
do
    echo "$var"
    if  [ "$var" == "--skipBuild" ]; then
      skip_build=true
    fi
    if  [ "$var" == "--clean" ]; then
      clean="clean"
    fi
    if  [ "$var" == "--docker" ]; then
      docker=true
    fi
done

build_status=0
if  ! $skip_build
then
  mvn -f log-message-generator/pom.xml $clean package
  build_status=$?
fi

if test $build_status -ne 0; then
  exit 1
fi


message_limit='-1' # run continously
read -e -p "[LogMessageGenerator] Enter the number of messages to generate(-1 to run continously): " -i "$message_limit" message_limit
message_rate='100.0' # 20 mps
read -e -p "[LogMessageGenerator] Enter the message generation rate (messages per second): " -i "$message_rate" message_rate
error_rate_limit='0.05' # 5 pct error rate
read -e -p "[LogMessageGenerator] Enter the error generation rate (percentage of overall messages): " -i "$error_rate_limit" error_rate_limit
error_delay="1500" # 1.5 second error message delay
read -e -p "[LogMessageGenerator] Enter the delay for errors to be sent after related non-error messages (in millis): " -i "$error_delay" error_delay

params="$message_limit $message_rate $error_rate_limit $error_delay"

cmd="java -Duser.timezone=UTC -cp $jar_name $LogMessageGenerator_class_name $params"
if ! $docker
then
  echo -e "The following command will be run:\n$cmd"
  read -n 1 -s -r -p "Press any key to continue"
  eval "time $cmd"
else
  echo "$cmd" > log-message-generator/run_log_message_generator.cmd
fi
