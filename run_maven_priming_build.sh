#!/usr/bin/env bash

mvn -f log-message-generator/pom.xml clean install \
&& mvn -f log-message-reducer-uber-jar/pom.xml clean install  \
&& mvn -f log-message-reducer/pom.xml clean install 
