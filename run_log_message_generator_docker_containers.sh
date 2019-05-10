#!/usr/bin/env bash

# clean things up first
cat log-message-generator/docker-compose-template.yml >log-message-generator/docker-compose.yml
rm -fv log-message-generator/*cmd

# get the log-message-generator set up and capture the runtime parameterized command
./run_log_message_generator.sh --docker
if [ $? -eq 0 ] && [ -f log-message-generator/run_log_message_generator.cmd ]; then
  cmd=$(cat log-message-generator/run_log_message_generator.cmd)
  sed -i "s#log-message-generator.cmd#$cmd#g" log-message-generator/docker-compose.yml
else
  echo -e "Something went wrong running run_log_message_generator.sh. Cannot continue"
  exit 1
fi

# get the log-message-generator set up and capture the runtime parameterized command
./run_log_message_splitter.sh --docker --skipBuild
if [ $? -eq 0 ] && [ -f log-message-generator/run_log_message_splitter.cmd ]; then
  cmd=$(cat log-message-generator/run_log_message_splitter.cmd)
  echo $cmd
  sed -i "s#log-message-splitter.cmd#$cmd#g" log-message-generator/docker-compose.yml
else
  echo -e "Something went wrong running run_log_message_splitter.sh. Cannot continue"
  exit 1
fi

# one litte fix for the container - log-message-generator path is not path relative, it is mapped to /log-message-generator
sed -i "s#-cp log-message-generator#-cp /log-message-generator#g" log-message-generator/docker-compose.yml

# now look for any references to generated or hardwired hostname:ips that need to be addressed in the container
regex_hostname_or_ip="(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])"
regex_port="[0-9]{4,5}"
regex_pattern="$regex_hostname_or_ip:$regex_port"
regex_matches=$(egrep -ohr "$regex_pattern" log-message-generator/*cmd log-message-generator/src/main/resources/log4j2.xml |sort -u)
if [ "${#regex_matches[@]}" -gt 0 ]; then
  echo -e "There are references to hostnames in the generated runtime artifacts that may need to be resolved for containers to reach them: \n$regex_matches"
  response="y"
  read -e -p "Proceed(y/n)? " -i "$response" response
  if [ "$response" == "y" ]; then
    ip="127.0.0.1"
    for each in $regex_matches; do
      hostname=$(echo $each| sed -E 's/(\S+):(\S+)/\1/')
      read -e -p "How about $hostname? What is the Ip number?: " -i "$ip" ip
      sed -i "/extra_hosts:/a - \"$hostname:$ip\"" log-message-generator/docker-compose.yml
    done
    sed -i "s/^-/    -/g" log-message-generator/docker-compose.yml # align the just-added references by adding some spaces
  else
    sed -i '/extra_hosts:/d' log-message-generator/docker-compose-template.yml # remove unnecssary hosts mapping 
  fi
fi
docker build -t=cleverfishsoftware.com/log-message-generator log-message-generator/

cmd="docker-compose -f log-message-generator/docker-compose.yml up -d"
echo -e "About to start container services using command: $cmd?"
response="y"
read -e -p "Proceed(y/n)? " -i "$response" response
if [ "$response" == "y" ]; then
  eval "$cmd"
fi
