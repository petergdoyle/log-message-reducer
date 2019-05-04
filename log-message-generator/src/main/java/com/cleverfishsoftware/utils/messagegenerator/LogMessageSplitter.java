/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.json.simple.parser.JSONParser;

/**
 *
 */
public class LogMessageSplitter {

    public static void main(String[] args) {

        String brokerList = "";
        boolean error = false;
        if (args == null || args.length == 0 || args.length < 1) {
            error = true;
        } else {
            brokerList = args[0];
        }

        if (error) {
            System.err.println("Usage LogMessageSplitter <broker-list>\n"
                    + "broker-list - the kafka brokers to bootstrap\n"
                    + "\n");
            System.exit(1);
        }

        String logRegex = "(\\[.+?\\])? (\\S+) (.+) (.+) - (.+)";
        Pattern logPattern = Pattern.compile(logRegex);

        // set up the consumer
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", brokerList);
        consumerProps.put("group.id", "LogMessageSplitter-cg");
        consumerProps.put("enable.auto.commit", "true");
        consumerProps.put("auto.commit.interval.ms", "1000");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList("logs"));

        // set up the producer
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", brokerList);
        producerProps.put("acks", "all");
        producerProps.put("retries", 0);
        producerProps.put("batch.size", 16384);
        producerProps.put("linger.ms", 1);
        producerProps.put("buffer.memory", 33554432);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(producerProps);

        Integer counter = 0;
        Map<String, Integer> counts = new HashMap<>();

        System.out.println("\n");
        JSONParser parser = new JSONParser();
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    String value = record.value();

//                    try {
//                        JSONObject json = (JSONObject) parser.parse(value);
//                        String level  = (String) json.get("level");
//                        String msg  = (String) json.get("message");
//                        switch (level) {
//                            case "ERROR":
//                                producer.send(new ProducerRecord("logs-stderr", value));
//                                Integer valErr = counts.get("STDERR");
//                                counts.put("STDERR", ((valErr != null) ? valErr : 0) + 1);
//                                break;
//                            default:
//                                producer.send(new ProducerRecord("logs-stdout", value));
//                                Integer valOut = counts.get("STDOUT");
//                                counts.put("STDOUT", ((valOut != null) ? valOut : 0) + 1);
//                        }
//                    } catch (ParseException ex) {
//                        System.err.printf("Cannot parse the record: %s\n", value);
//                    }
                    Matcher matcher = logPattern.matcher(value.trim());
                    if (matcher.matches()) {
                        String level = matcher.group(2);
                        String msg = matcher.group(5);
                        switch (level) {
                            case "ERROR":
                                producer.send(new ProducerRecord("logs-stderr", msg));
                                Integer valErr = counts.get("STDERR");
                                counts.put("STDERR", ((valErr != null) ? valErr : 0) + 1);
                                break;
                            default:
                                producer.send(new ProducerRecord("logs-stdout", msg));
                                Integer valOut = counts.get("STDOUT");
                                counts.put("STDOUT", ((valOut != null) ? valOut : 0) + 1);
                        }
                    } else {
                        System.err.printf("Cannot match the record: %s\n", value);
                    }

                    counter++;
                    System.out.printf("\r[KafkaTopicSplitter] Counter: %d Totals: %s", counter, counts);
                }
            }
        } catch (WakeupException ex) {
            // ignore
        } finally {
            consumer.close();
            producer.close();
        }

    }

}
