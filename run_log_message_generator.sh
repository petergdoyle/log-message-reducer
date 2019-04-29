#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.RunLogMessageGenerator'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'
log4j_properties="-Dlog4j.configuration=file:/path/to/log4j.properties"

log_file_name="logs/log.json" #this needs to map to what is in the log-4j appender config
rm -fv $log_file_name
sleep 1

# if [ -f $log_file_name ]; then
#   prompt="The generated log file already exists. Do you want to delete it? (y/n): "
#   default_value="y"
#   read -e -p "$(echo -e $prompt)" -i $default_value response
#   if [ "$response" == 'y' ]; then
#     rm -fv $log_file_name
#   fi
# fi

skip_build=false
clean=""
limit='-1' #run continously
message_rate='20.0'
error_rate_limit='0.05'
params="$limit $message_rate $error_rate_limit"

for var in "$@"
do
    echo "$var"
    if  [ "$var" == "--skipBuild" ]; then
      skip_build=true
    fi
    if  [ "$var" == "--clean" ]; then
      clean="clean"
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

cmd="time java -Duser.timezone=UTC -cp $jar_name $class_name $params"
echo "$cmd"
eval "$cmd"

if [ $? -eq 0 ]; then
  if [ -f $log_file_name ] && [ -s $log_file_name ]; then
   echo "TOTAL: $(wc -l <$log_file_name)" \
    && echo "TRACE: $(grep -c 'TRACE' $log_file_name)" \
    && echo "WARN: $(grep -c 'WARN' $log_file_name)" \
    && echo "INFO: $(grep -c 'INFO' $log_file_name)" \
    && echo "DEBUG: $(grep -c 'DEBUG' $log_file_name)" \
    && echo "ERROR: $(grep -c 'ERROR' $log_file_name)" \
    && echo "FATAL: $(grep -c 'FATAL' $log_file_name)"
  fi
fi
