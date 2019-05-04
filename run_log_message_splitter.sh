#!/usr/bin/env bash

class_name='com.cleverfishsoftware.utils.messagegenerator.LogMessageSplitter'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'

skip_build=false
for var in "$@"
do
    echo "$var"
    if  [ "$var" == "--skipBuild" ]; then
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

broker_list="engine1:9092"
params="$broker_list"

cmd="java -cp $jar_name $class_name $params"
echo "$cmd"
