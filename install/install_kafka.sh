#!/usr/bin/env bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root"
   exit 1
fi

java -version > /dev/null 2>&1
if [ $? -eq 127 ]; then
  echo -e "Jdk8 is not installed. Install Jdk8"
  exit 1
fi

if [ -z "$JAVA_HOME" ]; then
  echo -e "ENV variable JAVA_HOME is not set. JAVA_HOME must be set"
  exit 1
fi

if [ -z "$KAFKA_HOME" ]; then

  if [ ! -d /usr/scala ]; then
    mkdir -pv /usr/kafka
  fi

  curl -O https://archive.apache.org/dist/kafka/0.10.2.1/kafka_2.11-0.10.2.1.tgz
  tar -xvf kafka_2.11-0.10.2.1.tgz -C /usr/kafka/
  ln -s /usr/kafka/kafka_2.11-0.10.2.1/ /usr/kafka/default
  rm -fr kafka_2.11-0.10.2.1.tgz

  if [ ! -f /etc/profile.d/kafka.sh ]; then
    cat <<EOF >/etc/profile.d/kafka.sh
export KAFKA_HOME=/usr/kafka/default
export PATH=\$PATH:\$KAFKA_HOME/bin
EOF
  fi

fi
