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

# install maven 3
eval 'mvn -version' > /dev/null 2>&1
if [ $? -eq 127 ]; then

  maven_home="/usr/maven/default"
  download_url="https://www-eu.apache.org/dist/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz"

  echo "downloading $download_url..."
  if [ ! -d /usr/maven ]; then
    mkdir -pv /usr/maven
  fi

  cmd="curl -O $download_url \
    && tar -xvf apache-maven-3.6.0-bin.tar.gz -C /usr/maven \
    && ln -s /usr/maven/apache-maven-3.6.0 $maven_home \
    && rm -f apache-maven-3.6.0-bin.tar.gz"
  eval "$cmd"

  export MAVEN_HOME=$maven_home
  cat <<EOF >/etc/profile.d/maven.sh
export MAVEN_HOME=$MAVEN_HOME
export PATH=\$PATH:\$MAVEN_HOME/bin
EOF

else
  echo -e "apache-maven-3.6.0 already appears to be installed. skipping."
fi
