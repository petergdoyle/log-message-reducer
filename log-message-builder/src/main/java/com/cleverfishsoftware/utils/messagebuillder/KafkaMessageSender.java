/*
 */
package com.cleverfishsoftware.utils.messagebuillder;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 */
public class KafkaMessageSender {
    
    private final Properties properties;
    private final String topic;
    private final KafkaProducer<String, String> producer;

    public KafkaMessageSender(final Properties properties) {
        this.properties = properties;
        this.topic = properties.getProperty("topic");
        this.producer = new KafkaProducer<>(properties);
    }

    public void send(final String msg) {
        producer.send(new ProducerRecord<>(topic, msg));
    }
    
}
