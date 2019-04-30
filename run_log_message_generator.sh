#!/usr/bin/env bash
class_name='com.cleverfishsoftware.utils.messagegenerator.RunLogMessageGenerator'
jar_name='log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar'
log4j_properties="-Dlog4j.configuration=file:/path/to/log4j.properties"

if [ -d logs/ ]; then
  rm -fvr "logs/*"
  sleep 1
fi

#
# "Usage RunLogMessageGenerator <message-limit> <message-rate> <error-rate-limit> <error-delay>\n"
#                     + "message-limit - the number of messages to generate (-1 to run continously)\n"
#                     + "message-rate - the rate per second to generate messages\n"
#                     + "error-rate-limit - the max pct of errors to generate relative to overall messages generated\n"
#                     + "error-delay - the number of milliseconds to delay the error message from being generated\n"
#

skip_build=false
clean=""
message_limit='-1' # run continously
message_rate='20.0' # 20 mps
error_rate_limit='0.05' # 5 pct error rate
error_delay="1500" # 1.5 second error message delay

params="$message_limit $message_rate $error_rate_limit $error_delay"

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

# if [ $? -eq 0 ]; then
#   if [ -f $log_file_name ] && [ -s $log_file_name ]; then
#    echo "TOTAL: $(wc -l <$log_file_name)" \
#     && echo "TRACE: $(grep -c 'TRACE' $log_file_name)" \
#     && echo "WARN: $(grep -c 'WARN' $log_file_name)" \
#     && echo "INFO: $(grep -c 'INFO' $log_file_name)" \
#     && echo "DEBUG: $(grep -c 'DEBUG' $log_file_name)" \
#     && echo "ERROR: $(grep -c 'ERROR' $log_file_name)" \
#     && echo "FATAL: $(grep -c 'FATAL' $log_file_name)"
#   fi
# fi
