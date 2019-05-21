# log-message-reducer

A project to see the "join" function in Spark Structured Streaming. The goal is to reduce log messages coming from two topics, one to represent messages logged as error, and one to represent messages logged as other than error but some that are directly related to the errors by means of a tracking id.

![pic](log-message-reducer.jpg)

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
There is an ```install/``` directory included which has individual installation scripts for everything. There is a sequence of installation that is required. For each script, a new environment variable is set, so you need to run the scripts as root (or "```sudo su -```") and then exit that session and sudo back in in order to set these Environment variables for the next step. At the end, you should be able to see all of them set correctly with an ```env```command. You need these components all installed and environment variables set.
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
Messages generated will look like the following as formatted by log4j pattern <PatternLayout pattern="%d{[dd/MMM/yyyy:hh:mm:ss Z]} %-5p [%-7t] %F:%L - %m%n"/>. The Log Message Splitter (next step) is looking for this form in order to distinguish between Error and Non-Error messages.
```
[14/May/2019:03:46:28 +0000] WARN  [pool-2-thread-2] LogMessage.java:81 - {"level":"warn","trackId":"dc04a3a2-fdc3-4ff6-8d54-eb97695b1fc9","body":"omittam purus eius ne oporteat non pulvinar voluptatibus impetus fames","ts":"2019-05-14T15:46:28.904Z"}

[14/May/2019:03:46:28 +0000] DEBUG [pool-2-thread-2] LogMessage.java:78 - {"level":"debug","trackId":"dc04a3a2-fdc3-4ff6-8d54-eb97695b1fc9","body":"idque conubia senectus dico atqui postulant vix","ts":"2019-05-14T15:46:28.944Z"}

[14/May/2019:03:46:28 +0000] DEBUG [pool-2-thread-2] LogMessage.java:78 - {"level":"debug","trackId":"dc04a3a2-fdc3-4ff6-8d54-eb97695b1fc9","body":"noster inceptos natoque erroribus nascetur","ts":"2019-05-14T15:46:28.983Z"}

[14/May/2019:03:46:29 +0000] DEBUG [pool-2-thread-2] LogMessage.java:78 - {"level":"debug","trackId":"dc04a3a2-fdc3-4ff6-8d54-eb97695b1fc9","body":"nascetur suspendisse perpetua curae mattis molestie","ts":"2019-05-14T15:46:29.023Z"}

[14/May/2019:03:46:29 +0000] INFO  [pool-2-thread-2] LogMessage.java:84 - {"level":"info","trackId":"dc04a3a2-fdc3-4ff6-8d54-eb97695b1fc9","body":"doming omittam inani graecis dolore ipsum tritani","ts":"2019-05-14T15:46:29.063Z"}

```

### Run Log Message Splitter
The main purpose of the Log Message Generator is to create a stream of logging messages that have a tracking id where errors that come later, as indicated by a timestamp on the record, can be correlated by to those non-error messages. The Log Message Generator is capable of creating an Error message, then using the same tracking id, create other random message types and push them into the log stream, and eventually push the Error message into the same stream. Spark Structured SQL Streaming can do a join on the two kafka streams by topic one Error and on Non-Error so the purpose of the Log Message Splitter is to read from the logs stream and identify Error and Non-Error messages and push them into the associated logs-stderr and logs-stdout topic so that the Log Message Reducer can read from both topics, do a join on those message streams by tracking id and only write the messages related to the Error stream out to a logs-reduced topic. You'll see more about the Log Message Reducer in the next steps.
You will be prompted for the Kafka cluster details and then see console messages from both the embedded Kafka Consumer and Kafka Producer since the Log Message Splitter is taking from the logs topic and spltting then writing out to log-stdout or logs-stderr.
Near the end you will also see the command to run this with the parameters provided and you can reuse that later and you will also see runtime stats about what it is reading and writing to.
```
$ ./run_LogMessageSplitter.sh
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
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.996 s
[INFO] Finished at: 2019-05-14T11:59:43-04:00
[INFO] ------------------------------------------------------------------------
[LogMessageSplitter] Enter the Kafka Broker list: engine2:9092
[LogMessageSplitter] Enter the Kafka Logs Topic name to take from : logs
[LogMessageSplitter] Enter the Kafka Logs Error Topic name to write to: logs-stderr
[LogMessageSplitter] Enter the Kafka Logs Topic name to write to: logs-stdout
The following command will be run:
java -cp log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar com.cleverfishsoftware.utils.messagegenerator.LogMessageSplitter engine2:9092 logs logs-stderr logs-stdout
Press any key to continue[main] INFO org.apache.kafka.clients.consumer.ConsumerConfig - ConsumerConfig values:
	auto.commit.interval.ms = 1000
	auto.offset.reset = latest
	bootstrap.servers = [engine2:9092]
	check.crcs = true
	client.id =
	connections.max.idle.ms = 540000
	enable.auto.commit = true
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = LogMessageSplitter-cg
	heartbeat.interval.ms = 3000
	interceptor.classes = null
	key.deserializer = class org.apache.kafka.common.serialization.StringDeserializer
	max.partition.fetch.bytes = 1048576
	max.poll.interval.ms = 300000
	max.poll.records = 500
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partition.assignment.strategy = [class org.apache.kafka.clients.consumer.RangeAssignor]
	receive.buffer.bytes = 65536
	reconnect.backoff.ms = 50
	request.timeout.ms = 305000
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
	session.timeout.ms = 10000
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
	value.deserializer = class org.apache.kafka.common.serialization.StringDeserializer

[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka version : 0.10.2.1
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka commitId : e89bffd6b2eff799
[main] INFO org.apache.kafka.clients.producer.ProducerConfig - ProducerConfig values:
	acks = all
	batch.size = 16384
	block.on.buffer.full = false
	bootstrap.servers = [engine2:9092]
	buffer.memory = 33554432
	client.id =
	compression.type = none
	connections.max.idle.ms = 540000
	interceptor.classes = null
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 1
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
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka version : 0.10.2.1
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka commitId : e89bffd6b2eff799


[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - Discovered coordinator engine2:9092 (id: 2147483645 rack: null) for group LogMessageSplitter-cg.
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - Revoking previously assigned partitions [] for group LogMessageSplitter-cg
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - (Re-)joining group LogMessageSplitter-cg
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - Successfully joined group LogMessageSplitter-cg with generation 23
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - Setting newly assigned partitions [logs-1] for group LogMessageSplitter-cg
[KafkaTopicSplitter] Counter: 199 Totals: {STDERR=11, STDOUT=188}
```


### Optionally Run Log Message Generator and Log Message Splitter as managed Docker Services using Docker-Compose
Remember if you cancel running either the Log Message Generator or Log Message Splitter then they stop. You can keep them running with Docker and Docker-Compose. There is a script to set both utilities up and then launch containers to run in the background as daemon processes.
```
$ ./run_LogMessageGenerator_LogMessageSplitter_in_docker_containers.sh
--docker
‘log-message-generator/src/main/resources/log4j2.xml’ -> ‘log-message-generator/src/main/resources/log4j2-prev.xml’
[LogMessageGenerator] Specify Log4j Appender ('stdout'|'kafka'|'file'): kafka
‘log-message-generator/src/main/resources/log4j2-kafka-template.xml’ -> ‘log-message-generator/src/main/resources/log4j2.xml’
[LogMessageGenerator] Enter the Kafka Broker list: engine2:9092
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
[INFO] Total time:  2.567 s
[INFO] Finished at: 2019-05-14T12:06:45-04:00
[INFO] ------------------------------------------------------------------------
[LogMessageGenerator] Enter the number of messages to generate (-1 to run continously): -1
[LogMessageGenerator] Enter the message generation rate (messages per second): 20.0
[LogMessageGenerator] Enter the error generation rate (percentage of overall messages): 0.05
[LogMessageGenerator] Enter the delay for errors to be sent after related non-error messages (in millis): 1500
--docker
--skipBuild
[LogMessageSplitter] Enter the Kafka Broker list: engine2:9092
[LogMessageSplitter] Enter the Kafka Logs Topic name to take from : logs
[LogMessageSplitter] Enter the Kafka Logs Error Topic name to write to: logs-stderr
[LogMessageSplitter] Enter the Kafka Logs Topic name to write to: logs-stdout
java -cp log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar com.cleverfishsoftware.utils.messagegenerator.LogMessageSplitter engine2:9092 logs logs-stderr logs-stdout
Sending build context to Docker daemon   8.73MB
Step 1/5 : FROM centos
 ---> 9f38484d220f
Step 2/5 : ENV JAVA_HOME /usr/java/default
 ---> Using cache
 ---> ac0f77adfef5
Step 3/5 : RUN yum -y install java-1.8.0-openjdk-headless && yum -y install java-1.8.0-openjdk-devel
 ---> Using cache
 ---> 65bcfbfe3b1d
Step 4/5 : COPY target/lib /log-message-generator/target/lib
 ---> Using cache
 ---> 8cb5bac48a26
Step 5/5 : COPY target/log-message-generator-1.0-SNAPSHOT.jar /log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar
 ---> 47c91357af05
Successfully built 47c91357af05
Successfully tagged cleverfishsoftware.com/log-message-generator:latest
About to start container services using command: docker-compose -f log-message-generator/docker-compose.yml up -d?
Proceed(y/n)? y
Creating log-message-generator_LogMessageGenerator-service_1 ... done
Creating log-message-generator_LogMessageSplitter-service_1  ... done
```
To shut them down you can destroy them with the docker-compose command
```
$ docker-compose -f log-message-generator/docker-compose.yml down
```

Note that the Log Message Splitter also strips off the Log4j information other than the message body. So the messages going to logs-stdout and logs-stderr look like this. Notice that all that is left is the JSON body, so that it can be treated as a JSON message in the Log Message Reducer.
```
{"level":"fatal","trackId":"5ceb02cd-5adb-4a6f-ac8c-c1e05cfd5fcb","body":"fastidii velit pro habeo suas lacus eirmod menandri eam","ts":"2019-05-14T16:14:28.104Z"}
{"level":"trace","trackId":"5ceb02cd-5adb-4a6f-ac8c-c1e05cfd5fcb","body":"tristique magnis suavitate definiebas equidem maximus volutpat interpretaris deterruisset","ts":"2019-05-14T16:14:28.143Z"}
{"level":"debug","trackId":"5ceb02cd-5adb-4a6f-ac8c-c1e05cfd5fcb","body":"nihil taciti eloquentiam mea sea legere lacus reformidans omnesque","ts":"2019-05-14T16:14:28.183Z"}
{"level":"trace","trackId":"5ceb02cd-5adb-4a6f-ac8c-c1e05cfd5fcb","body":"iuvaret eros orci sit eleifend sanctus integer invidunt eripuit eros epicuri consul mei constituto","ts":"2019-05-14T16:14:28.223Z"}

```

### Run Log Message Reducer
The Log Message Reducer is the meat of the matter here, the rest is just stand up for getting the streaming Error and Non-Error streams up and running. Let's take a look at the code. It is written in Scala and is very declarative.
- Create a SparkSession
- Create a Struct to hold the parsed JSON message data
- Create a Spark streaming context for both the logs-stderr and logs-stdout topics to be read from
- Join on the trackId field
- Write the joined streams out to a single logs-reduced topic

Spark Structured Streaming Watermarks
Watermarks is a threshold , which defines the how long we wait for the late events. Combining watermarks with automatic source time tracking ( event time) spark can automatically drop the state and keep it in bounded way. When you enable watermarks, for a specific window starting at time T, spark will maintain state and allow late data to update the state until (max event time seen by the engine - late threshold > T). In other words, late data within the threshold will be aggregated, but data later than the threshold will be dropped.

Spark Structured Streaming Watermarks combined with an Event Timestamp.
By defining a timestamp field in the Stuct type and parsing it, the Watermark or (window) will use Event Time rather than Arrival time to keep within the Watermark bounds.
```

    val spark = SparkSession
      .builder
      .appName("LogMessageReducer")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._
    import org.apache.spark.sql.streaming.{OutputMode, Trigger}
    import org.apache.spark.sql.types.{DataTypes, StructType}
    import scala.concurrent.duration._

    val schema = new StructType()
      .add("level", DataTypes.StringType)
      .add("trackId", DataTypes.StringType)
      .add("body", DataTypes.StringType)
      .add("ts", DataTypes.StringType)

    val stdOutDf = spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", consumerTopicStdOut)
        .option("startingOffsets", "latest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)") // take the "value" field from the Kafka ConsumerRecord
        .select(from_json($"value", schema) as("stdout")) // convert to json objects
        .select("stdout.*")


    val stdErrDf = spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", consumerTopicStdErr)
        .option("startingOffsets", "latest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)") // take the "value" field from the Kafka ConsumerRecord
        .select(from_json($"value", schema) as("stderr")) // convert to json objects
        .select("stderr.*")


    val joinedDf = stdErrDf.alias("err").join(stdOutDf.alias("out"),Seq("trackId"))
        // val joinedDf = stdErrDf.withWatermark(“eventTime1”, “10 seconds).join(stdOutDf,"trackId")
        .select("out.level","err.trackId","out.body","out.ts")

    joinedDf
      .selectExpr("to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("topic", producerReducedTopic)
      .outputMode("append")
      .option("checkpointLocation",s"$checkpointDir/reducer")
      .start()
      .awaitTermination()

```

### Submitting the Job to Spark Standalone
The script here submits jobs to a Spark Standalone cluster. Spark is installed but is not running yet as a cluster. If you want to setup and configure a Spark Standalone cluster then clone spark-standalone-quick from https://github.com/petergdoyle/spark-standalone-quick and follow the scripts there to configure spark standalone with either a single node or mult-node configuration.
Once done you can submit the spark Log Message Reducer and check the results (next step)
```
$ ./spark_submit_LogMessageReducer.sh
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< com.cleverfishsoftware:log-message-reducer >-------------
[INFO] Building log-message-reducer 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ log-message-reducer ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /vagrant/log-message-reducer/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ log-message-reducer ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- scala-maven-plugin:4.0.1:compile (default) @ log-message-reducer ---
[WARNING]  Expected all dependencies to require Scala version: 2.11.-1
[WARNING]  com.cleverfishsoftware:log-message-reducer:1.0-SNAPSHOT requires scala version: 2.11.11
[WARNING]  org.json4s:json4s-core_2.10:3.2.10 requires scala version: 2.10.0
[WARNING] Multiple versions of scala libraries detected!
[INFO] Using incremental compilation using Mixed compile order
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ log-message-reducer ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /vagrant/log-message-reducer/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ log-message-reducer ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-surefire-plugin:2.21.0:test (default-test) @ log-message-reducer ---
[INFO] Tests are skipped.
[INFO]
[INFO] --- scalatest-maven-plugin:2.0.0:test (test) @ log-message-reducer ---
Discovery starting.
Discovery completed in 361 milliseconds.
Run starting. Expected test count is: 0
DiscoverySuite:
Run completed in 515 milliseconds.
Total number of tests run: 0
Suites: completed 1, aborted 0
Tests: succeeded 0, failed 0, canceled 0, ignored 0, pending 0
No tests were executed.
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ log-message-reducer ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.854 s
[INFO] Finished at: 2019-05-14T12:50:19-04:00
[INFO] ------------------------------------------------------------------------
[LogMessageReducer] Enter the Kafka Broker list: localhost:9092
[LogMessageReducer] Enter the Kafka Consumer Group name: LogMessageReducer-cg
[LogMessageReducer] Enter the topic name for STDOUT messages: logs-stdout
[LogMessageReducer] Enter the topic name for STDERR messages: logs-stderr
[LogMessageReducer] Enter the topic name for the Joined messages: logs-reduced
[LogMessageReducer] Enter spark url for the Spark Master Node: spark://engine1:7077
[LogMessageReducer] Enter spark deployment mode (local/cluster) to run the Driver Program: local
The following command will be run:
spark-submit --driver-java-options "-Dlog4j.configuration=file:///vagrant/spark_log4j_QUIET.properties"  --master spark://engine1:7077 --supervise  --jars /vagrant/log-message-reducer-uber-jar/target/log-message-reducer-uber-jar-1.0-SNAPSHOT-jar-with-dependencies.jar --class com.cleverfishsoftware.challenge.scala.LogMessageReducer /vagrant/log-message-reducer/target/log-message-reducer-1.0-SNAPSHOT.jar localhost:9092 LogMessageReducer-cg logs-stdout logs-stderr logs-reduced /tmp/spark/checkpoint
Press any key to continue
```
### Seeing the results
To see the logs stream of data (from Log Message Generator) - substituting broker1 for the correct one.
```
$ kafka-console-consumer.sh --bootstrap-server broker1:9092 --topic logs
```

To see the logs-stdout and logs-stderr streams of data (from Log Message Splitter)
```
$ kafka-console-consumer.sh --bootstrap-server broker1:9092 --topic logs-stdout
...
$ kafka-console-consumer.sh --bootstrap-server broker1:9092 --topic logs-stderr
...
```
and of course the output of the Log Message Reducer
```
$ kafka-console-consumer.sh --bootstrap-server broker1:9092 --topic logs-reduced 
```
