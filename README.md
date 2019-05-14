# log-message-reducer



## Required Environment:
### Java Jdk 8
### Maven 3.6
### Scala 2.11
### Kafka 2.11-0.10.2.1
### Spark 2.4.1
### Docker & Docker Compose (optional)

## Setup Options
# Run Virtualbox/Vagrant
A Vagrantfile is included that will build a CentOS VM and provision it with all the aforementioned Required Environment items.

# Install Manually
There is an ```install/``` directory included which has individual installation scripts for everything. There is a sequence of installation that is required. For each script, a new environment variable is set, so you need to run the scripts as root (or "sudo su -") and then exit that session and sudo back in in order to set these Environment variables for the next step. At the end, you should be able to all of them. You need these components all installed and environment variables set.
```
$ env |grep HOME
KAFKA_HOME=/vagrant/kafka-proxied/local/kafka/default
SPARK_HOME=/usr/spark/default
MAVEN_HOME=/usr/maven/default
JAVA_HOME=/usr/java/default
SCALA_HOME=/usr/scala/default
```
# Use an existing installation
You can pick and choose anything that is missing but in the end you will still need the same environment variables set to run the operational scripts. 

**Note: that Spark 2.4.1 had dependencies with security vulnerabilities at the time this was put together. Moving to Spark 2.4.2 resolves some of those issues but seems to bring in a logging dependency issue - Since this is NOT intended to be a Production configuration/runtime, I choose the 2.4.1 path for now and will resolve the 2.4.2 issues later.**

## Steps

# Install all dependencies
Make sure you install each of the following

# Run Maven Priming Build
There are three projects that need to be fully built first so that all dependencies are pulled down and assembled. You can go an do a full ```mvn clean install``` in each of the three project directories (log-message-generator/, log-message-reducer-uber-jar/, log-message-reducer/) or just run the following script to do the same.  
```
$ ./run_maven_priming_build.sh
```


# Run Log Message Generator
```
```


# Run Log Message Splitter
```
```


# Optionally Run Log Message Generator and Log Message Splitter as managed Docker Services using Docker-Compose
```
```
