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

  # config.vm.box = "centos/7"
  config.vm.box = "petergdoyle/CentOS-7-x86_64-Minimal-1511"
  config.ssh.insert_key = false

  # config.vm.network "forwarded_port", guest: 8080, host: 8080 # spark master web ui port
  # config.vm.network "forwarded_port", guest: 8081, host: 8081 # spark worker web ui port
  # config.vm.network "forwarded_port", guest: 4040, host: 4040 # spark job web ui port


  config.vm.provider "virtualbox" do |vb|
 #   vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--cpuexecutioncap", "80"]
    vb.cpus=4
    vb.memory = "4096"
  end

  config.vm.hostname = "log-message-reducer.cleverfishsoftware.com"

  config.vm.provision "shell", inline: <<-SHELL

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

    spark_home="/usr/spark/default"
    download_url="https://www-us.apache.org/dist/spark/spark-2.4.1/spark-2.4.1-bin-hadoop2.7.tgz"

    if [ ! -d /usr/spark ]; then
      mkdir -pv /usr/spark
    fi

    echo "downloading $download_url..."
    cmd="curl -O $download_url \
      && tar -xvf  spark-2.4.1-bin-hadoop2.7.tgz -C /usr/spark \
      && ln -s /usr/spark/spark-2.4.1-bin-hadoop2.7 $spark_home \
      && rm -f spark-2.4.1-bin-hadoop2.7.tgz"
    eval "$cmd"

        export SPARK_HOME=$spark_home
        cat <<EOF >>/etc/profile.d/spark.sh
export SPARK_HOME=$SPARK_HOME
export PATH=\\\$PATH:\\\$SPARK_HOME/bin
EOF
    # spark nodes need a checkpoint directory to keep state should a node go down
    if [ ! -d "/spark/checkpoint" ]; then
      mkdir -p "/spark/checkpoint"
      chmod ugo+rw "/spark/checkpoint/"
    fi

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

  # yum -y install rh-python36
  # scl enable rh-python36 bash
  # yum -y python36-pip
  # pip3 install numpy pandas

  # install any additional packages
  yum -y install net-tools telnet git htop

  yum -y update

  SHELL
end
