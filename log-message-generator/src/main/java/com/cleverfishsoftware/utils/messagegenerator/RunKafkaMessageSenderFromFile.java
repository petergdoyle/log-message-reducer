/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

/**
 */
public class RunKafkaMessageSenderFromFile {

    public static void main(String[] args) throws IOException {

        String fileName = "";
        String brokerList = "";
        String logsTopic = "";
        boolean error = false;
        if (args == null || args.length == 0 || args.length < 2) {
            error = true;
        } else {
            fileName = args[0];
            brokerList = args[1];
            logsTopic = args[2];
        }

        if (error) {
            System.err.println("Usage RunKafkaMessageSenderFromFile <filename> <broker-list> <logs-topic>\n"
                    + "filename - the file to read from\n"
                    + "broker-list - the kafka brokers to bootstrap\n"
                    + "logs-topic - the kafka topic name to write to\n"
                    + "\n");
            System.exit(1);
        }

        // set up the producer
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("topic", logsTopic);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaMessageSender kafkaMessageSender = new KafkaMessageSender(props);

        File file = new File(fileName);
        Scanner input = new Scanner(file);
        while (input.hasNextLine()) {
            String nextLine = input.nextLine();
            kafkaMessageSender.send(nextLine);
        }

    }

}
