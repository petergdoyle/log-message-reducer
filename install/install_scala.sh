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

# install scala 2.11
eval 'scala -version' > /dev/null 2>&1
if [ $? -eq 127 ]; then

  scala_home="/usr/scala/default"
  download_url="https://downloads.lightbend.com/scala/2.11.12/scala-2.11.12.tgz"

  if [ ! -d /usr/scala ]; then
    mkdir -pv /usr/scala
  fi

  echo "downloading $download_url..."
  cmd="curl -O $download_url \
    && tar -xvf  scala-2.11.12.tgz -C /usr/scala \
    && ln -s /usr/scala/scala-2.11.12 $scala_home \
    && rm -f scala-2.11.12.tgz"
  eval "$cmd"

      export SCALA_HOME=$scala_home
      cat <<EOF >/etc/profile.d/scala.sh
export SCALA_HOME=$SCALA_HOME
export PATH=\$PATH:\$SCALA_HOME/bin
EOF

else
  echo -e "scala-2.11 already appears to be installed. skipping."
fi
