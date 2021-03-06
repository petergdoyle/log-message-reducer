# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.

  config.vm.box = "centos/7"
  # config.vm.box = "petergdoyle/CentOS-7-x86_64-Minimal-1511"
  config.ssh.insert_key = false

  # config.vm.network "forwarded_port", guest: 8080, host: 8080 # spark master web ui port
  # config.vm.network "forwarded_port", guest: 8081, host: 8081 # spark worker web ui port
  # config.vm.network "forwarded_port", guest: 4040, host: 4040 # spark job web ui port

  config.vm.provider "virtualbox" do |vb|
    vb.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
    vb.cpus=4
    vb.memory = "4096"
  end

  config.vm.hostname = "log-message-reducer.cleverfishsoftware.com"

  config.vm.provision "shell", inline: <<-SHELL

  # install docker and docker-compose

  eval 'docker --version' > /dev/null 2>&1
  if [ $? -eq 127 ]; then
    echo "installing docker and docker-compose..."

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

    yum -y install python-pip
    pip install --upgrade pip
    pip install -U docker-compose

    usermod -aG docker vagrant

  else
    echo "docker and docker-compose already installed"
  fi

  # install kafka-proxied for kafka setup and run scripts
  if [ ! -d "/vagrant/kafka-proxied" ]; then
    cd /vagrant
    git clone https://github.com/petergdoyle/kafka-proxied.git
    cd -
  fi

  # install openjdk-8
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
    cat <<EOF >>/etc/profile.d/maven.sh
export MAVEN_HOME=$MAVEN_HOME
export PATH=\\\$PATH:\\\$MAVEN_HOME/bin
EOF

  else
    echo -e "apache-maven-3.6.0 already appears to be installed. skipping."
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
        cat <<EOF >>/etc/profile.d/scala.sh
export SCALA_HOME=$SCALA_HOME
export PATH=\\\$PATH:\\\$SCALA_HOME/bin
EOF

  else
    echo -e "scala-2.11 already appears to be installed. skipping."
  fi

  # install spark 2.11
  eval 'spark-submit --version' > /dev/null 2>&1
  if [ $? -eq 127 ]; then

    spark_version='spark-2.4.1'
    spark_home="/usr/spark/default"
    download_url="https://archive.apache.org/dist/spark/$spark_version/$spark_version-bin-hadoop2.7.tgz"

    if [ ! -d /usr/spark ]; then
      mkdir -pv /usr/spark
    fi

    echo "downloading $download_url..."
    cmd="curl -O $download_url \
      && tar -xvf  $spark_version-bin-hadoop2.7.tgz -C /usr/spark \
      && ln -s /usr/spark/$spark_version-bin-hadoop2.7 $spark_home \
      && rm -f $spark_version-bin-hadoop2.7.tgz"
    eval "$cmd"

    cat <<EOF >/etc/profile.d/spark.sh
export SPARK_HOME=$spark_home
export PATH=\$PATH:\$SPARK_HOME/bin
EOF

  else
    echo -e "spark-2.11 already appears to be installed. skipping."
  fi

  # modify environment for vagrant user
  if ! grep -q '^alias cd' /home/vagrant/.bashrc; then
    echo 'alias cd="HOME=/vagrant cd"' >> /home/vagrant/.bashrc
  fi
  if ! grep -q '^alias ll' /home/vagrant/.bashrc; then
    echo 'alias ll="ls -lh"' >> /home/vagrant/.bashrc
  fi
  if ! grep -q '^alias netstat' /home/vagrant/.bashrc; then
    echo 'alias netstat="sudo netstat -tulpn"' >> /home/vagrant/.bashrc
  fi

  # install python and helpful libs
  # yum -y install rh-python36
  # scl enable rh-python36 bash
  # yum -y python36-pip
  # pip3 install numpy pandas

  # install any additional packages
  yum -y install net-tools telnet git htop iftop iptraf-ng.x86_64

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

  yum -y update

  SHELL
end
