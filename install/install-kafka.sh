#!/usr/bin/env bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root"
   exit 1
fi

java -version > /dev/null 2>&1
if [ $? -eq 127 ]; then
  display_error "Jdk8 is not installed. Install Jdk8"
  exit 1
fi

if [ -z "$JAVA_HOME" ]; then
  display_error "ENV variable JAVA_HOME is not set. JAVA_HOME must be set"
  exit 1
fi

if [ ! -d "/vagrant/kafka-proxied" ]; then
  cd /vagrant
  git clone https://github.com/petergdoyle/kafka-proxied.git
  cd -
fi
