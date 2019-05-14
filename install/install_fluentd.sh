#!/usr/bin/env bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root"
   exit 1
fi

# install/configure pre-reqs for fluentd
# verify with fluentd installation guide https://docs.fluentd.org/v0.12/articles/before-install

yum -y install ntp
if [ ! $(grep '^root.*fluentd pre-req$' /etc/security/limits.conf| wc -l) == "2" ]; then
  # add the following 2 lines specifically tagged as a fluentd pre-req
  cat <<EOF >>/etc/security/limits.conf
root soft nofile 65536 # fluentd pre-req
root hard nofile 65536 # fluentd pre-req
EOF
else
  echo "fluentd pre-reqs for max file descriptors already installed - no changes required"
fi

if [ ! $(grep '^net.*fluentd pre-req$' /etc/sysctl.conf| wc -l) == "10" ]; then
  # add the following 10 lines specifically tagged as a fluentd pre-req
  cat <<EOF >>/etc/sysctl.conf
net.core.somaxconn = 1024 # fluentd pre-req
net.core.netdev_max_backlog = 5000 # fluentd pre-req
net.core.rmem_max = 16777216 # fluentd pre-req
net.core.wmem_max = 16777216 # fluentd pre-req
net.ipv4.tcp_wmem = 4096 12582912 16777216 # fluentd pre-req
net.ipv4.tcp_rmem = 4096 12582912 16777216 # fluentd pre-req
net.ipv4.tcp_max_syn_backlog = 8096 # fluentd pre-req
net.ipv4.tcp_slow_start_after_idle = 0 # fluentd pre-req
net.ipv4.tcp_tw_reuse = 1 # fluentd pre-req
net.ipv4.ip_local_port_range = 10240 65535 # fluentd pre-req
EOF
else
  echo "fluentd pre-reqs for network kernel parameters already installed - no changes required"
fi
