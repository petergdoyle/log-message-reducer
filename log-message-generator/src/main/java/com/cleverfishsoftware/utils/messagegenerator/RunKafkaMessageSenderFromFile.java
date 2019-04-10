/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 */
public class RunKafkaMessageSenderFromFile {

    public static void main(String[] args) throws IOException {

        //create a kafka producer
        InputStream resourceAsStream = RunKafkaMessageSenderFromFile.class.getClassLoader().getResourceAsStream("kafka-0.10.2.1-producer.properties");
        Properties kafkaProducerProperties = new Properties();
        kafkaProducerProperties.load(resourceAsStream);
        KafkaMessageSender kafkaMessageSender = new KafkaMessageSender(kafkaProducerProperties);

        String filename = args[0];
        File file = new File(filename);
        Scanner input = new Scanner(file);
        while (input.hasNextLine()) {
            String nextLine = input.nextLine();
            kafkaMessageSender.send(nextLine);
        }

    }

}
