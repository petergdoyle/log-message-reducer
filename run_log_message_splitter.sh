mvn -f log-message-generator/pom.xml package && java -cp log-message-generator/target/log-message-generator-1.0-SNAPSHOT.jar com.cleverfishsoftware.utils.messagegenerator.KafkaTopicSplitter
