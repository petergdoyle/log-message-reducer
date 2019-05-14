#!/usr/bin/env bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root"
   exit 1
fi

eval 'docker --version' > /dev/null 2>&1
if [ $? -eq 127 ]; then
  echo "installing docker and docker-compose..."
  yum -y install epel-release
  yum -y remove docker docker-common  docker-selinux docker-engine
  yum -y install yum-utils device-mapper-persistent-data lvm2
  yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  rm -fr /etc/yum.repos.d/docker.repo
  yum-config-manager --enable docker-ce-edge
  yum-config-manager --enable docker-ce-test
  yum -y makecache fast
  yum -y install docker-ce

  systemctl start docker
  systemctl enable docker
  groupadd docker

else
  echo "docker already installed"
fi

eval 'docker-compose --version' > /dev/null 2>&1
if [ $? -eq 127 ]; then
  yum -y install python-pip
  pip install --upgrade pip
  pip install -U docker-compose

  usermod -aG docker vagrant

else
  echo "docker-compose already installed"
fi
