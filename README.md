# log-message-reducer



## Required Environment:
### Java Jdk 8
### Maven 3.6
### Scala 2.11
### Kafka 2.11-0.10.2.1 (tested with this, later versions may work the same)
### Spark 2.4.1
### Docker & Docker Compose (optional)

## Setup Options
### Run Virtualbox/Vagrant
A Vagrantfile is included that will build a CentOS VM and provision it with all the aforementioned Required Environment items.

### Install Manually
There is an ```install/``` directory included which has individual installation scripts for everything. There is a sequence of installation that is required. For each script, a new environment variable is set, so you need to run the scripts as root (or "sudo su -") and then exit that session and sudo back in in order to set these Environment variables for the next step. At the end, you should be able to all of them. You need these components all installed and environment variables set.
```
$ env |grep HOME
KAFKA_HOME=/vagrant/kafka-proxied/local/kafka/default
SPARK_HOME=/usr/spark/default
MAVEN_HOME=/usr/maven/default
JAVA_HOME=/usr/java/default
SCALA_HOME=/usr/scala/default
```
### Use an existing installation
You can pick and choose anything that is missing but in the end you will still need the same environment variables set to run the operational scripts. You will need to set these individually and appropriately for your installation.

**Note: that Spark 2.4.1 had dependencies with security vulnerabilities at the time this was put together. Moving to Spark 2.4.2 resolves some of those issues but seems to bring in a logging dependency issue - Since this is NOT intended to be a Production configuration/runtime, I choose the 2.4.1 path for now and will resolve the 2.4.2 issues later.**

## Pre-requisite Steps

### Install all dependencies
Make sure you install each of the following as outlined above.

### Run Maven Priming Build
There are three projects that need to be fully built first so that all dependencies are pulled down and assembled. You can go an do a full ```mvn clean install``` in each of the three project directories (```log-message-generator/, log-message-reducer-uber-jar/, log-message-reducer/```) or just run the following script to do the same.  
```
$ ./run_maven_priming_build.sh
```

### Create Kafka Topics
You need to have a running Kafka Cluster or standalone server up and running. Installing Kafka as outlined before only will install Kafka client libraries and utilities on the client machine. If you intend on using the same machine or to stand up a small Kafka cluster for development and testing then the recommendation is to clone another repo with the scripts required to setup and configure Kafka.
Clone "kafka-cluster-quick" and follow the instructions there. There are scripts to stand up and tear down a single or multi-node cluster.
```
$ git clone https://github.com/petergdoyle/kafka-cluster-quick.git
```
Once a Kafka cluster is available, you need to create the required topics. There is a script to do that in this project. You will be prompted for the cluster details, specifically for the Zookeeper address and then it will create the required topics to continue here.
```
./kafka_create_topics.sh
```
Once topics are created, you can verify by checking
```
./kafka_list_topics.sh
```
There are other scripts and functions in the kafka-cluster-quick repo if you are going to manage your own cluster. Example to check the status of Kafka on a single Node. This will show you process numbers of the Zookeeper and Brokers and make sure things are okay. You can also check out logs under ```/usr/kafka/default/logs"``` or where ever ```$KAFKA_HOME``` is set.
```
kafka-cluster-quick/kafka_check_status.sh
```

## Running the code

### Run Log Message Generator
There is a utility class under the ```log-message-generator``` project that will create load on the Kafka cluster pushing log4j2 created log messages directly at a ```logs``` topic. You can start this up and configure it to run at whatever rate and limits you want.

```
$ ./run_LogMessageGenerator.sh
‘log-message-generator/src/main/resources/log4j2.xml’ -> ‘log-message-generator/src/main/resources/log4j2-prev.xml’
[LogMessageGenerator] Specify Log4j Appender ('stdout'|'kafka'|'file'): kafka
```
Choose stdout to see sample messages put to console. Choose file to see sample messages written to a log file. Choose kafka to see messages put to Kafka. If you choose kafka, additional details are required. When you are done setting all the runtime properties the script will echo the command to run the load generator. You can copy and paste this for future use rather than running through the script again, that is if nothing else changes for how you want to run the code.
```
$ ./run_LogMessageGenerator.sh
‘log-message-generator/src/main/resources/log4j2.xml’ -> ‘log-message-generator/src/main/resources/log4j2-prev.xml’
[LogMessageGenerator] Specify Log4j Appender ('stdout'|'kafka'|'file'): kafka
‘log-message-generator/src/main/resources/log4j2-kafka-template.xml’ -> ‘log-message-generator/src/main/resources/log4j2.xml’
[LogMessageGenerator] Enter the Kafka Broker list: localhost:9092
[LogMessageGenerator] Enter the Kafka Logs Topic name to take from : logs
A build is required to make these changes...

[INFO] Scanning for projects...
[INFO]
[INFO] ---------< com.cleverfishsoftware.utils:log-message-generator >---------
[INFO] Building log-message-generator 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ log-message-generator ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 6 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ log-message-generator ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ log-message-generator ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /vagrant/log-message-generator/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ log-message-generator ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ log-message-generator ---
[INFO]
[INFO] --- maven-jar-plugin:3.1.1:jar (default-jar) @ log-message-generator ---
[INFO] Building jar: /vagrant/log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.789 s
[INFO] Finished at: 2019-05-14T11:31:44-04:00
[INFO] ------------------------------------------------------------------------
[LogMessageGenerator] Enter the number of messages to generate (-1 to run continously): -1
[LogMessageGenerator] Enter the message generation rate (messages per second): 100.0
[LogMessageGenerator] Enter the error generation rate (percentage of overall messages): 0.05
[LogMessageGenerator] Enter the delay for errors to be sent after related non-error messages (in millis): 1500
The following command will be run:
java -Duser.timezone=UTC -cp log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar com.cleverfishsoftware.utils.messagegenerator.LogMessageGenerator -1 100.0 0.05 1500
Press any key to continue
```
Once running you can see details about the load and information about the embedded kafka producer as it runs. Near the end you can see how many messages are created and the type of messages. Depending on how you specified the error ratio, you will see errors show up accordingly. 
```
[main] INFO org.apache.kafka.clients.producer.ProducerConfig - ProducerConfig values:
	acks = 1
	batch.size = 0
	block.on.buffer.full = false
	bootstrap.servers = [localhost:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.fetch.timeout.ms = 60000
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 0
	retry.backoff.ms = 100
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.endpoint.identification.algorithm = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLS
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	timeout.ms = 30000
	value.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer

[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka version : 0.10.2.1
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka commitId : e89bffd6b2eff799


[INFO] generating 2147483647 log messages throttled at a rate of 100 per second, with an error-rate of 0.05 pct and an error delay of 1500 milliseconds. it should take aproximately 21474836.0 seconds to complete...

[LogMessageGenerator] Total: 934 {warn=161, trace=193, debug=176, error=47, fatal=171, info=186}
```


### Run Log Message Splitter
```
```


### Optionally Run Log Message Generator and Log Message Splitter as managed Docker Services using Docker-Compose
```
```
