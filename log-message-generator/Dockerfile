FROM centos

ENV JAVA_HOME /usr/java/default

RUN yum -y install java-1.8.0-openjdk-headless \
&& yum -y install java-1.8.0-openjdk-devel

COPY target/lib /log-message-generator/target/lib
COPY target/log-message-generator-1.0-SNAPSHOT.jar /log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar
