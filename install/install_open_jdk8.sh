#!/usr/bin/env bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root"
   exit 1
fi

eval java -version > /dev/null 2>&1
if [ $? -eq 127 ]; then

  mkdir -pv /usr/java
  yum -y install java-1.8.0-openjdk-headless && yum -y install java-1.8.0-openjdk-devel
  java_home=`alternatives --list |grep jre_1.8.0_openjdk| awk '{print $3}'`
  ln -s "$java_home" /usr/java/default

  export JAVA_HOME='/usr/java/default'
  cat >/etc/profile.d/java.sh <<-EOF
export JAVA_HOME=$JAVA_HOME
EOF

  # register all the java tools and executables to the OS as executables
  install_dir="$JAVA_HOME/bin"
  for each in $(find $install_dir -executable -type f) ; do
    name=$(basename $each)
    alternatives --install "/usr/bin/$name" "$name" "$each" 99999
  done

else
  echo -e "openjdk-8 already appears to be installed."
fi
