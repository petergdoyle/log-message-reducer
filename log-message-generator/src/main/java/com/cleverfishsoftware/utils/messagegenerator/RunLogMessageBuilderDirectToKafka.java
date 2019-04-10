/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import org.apache.logging.log4j.LogManager;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 */
public class RunLogMessageBuilderDirectToKafka {

    public static void main(String[] args) throws IOException {
        
        
        //create a kafka producer
        InputStream resourceAsStream = RunLogMessageBuilderDirectToKafka.class.getClassLoader().getResourceAsStream("kafka-0.10.2.1-producer.properties");
        Properties kafkaProducerProperties = new Properties();
        kafkaProducerProperties.load(resourceAsStream);
        KafkaMessageSender kafkaMessageSender = new KafkaMessageSender(kafkaProducerProperties);

        int limit = 0;
        boolean error = false;
        if (args == null || args.length == 0) {
            error = true;
        } else {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                error = true;
            }
        }
        if (error) {
            System.err.println("Usage RunLogMessageBuilderDirectToKafka <size>\n"
                    + "size - the number of messages to write to file \n\n");
            System.exit(1);
        }
        // each class must declare it's own logger and pass it to the LogBuilder or else we lose class level log scope
        org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RunLogMessageBuilderDirectToKafka.class.getName());
        Lorem lorem = LoremIpsum.getInstance();
        Random random = new Random();
        int randWordLenMin = 5;
        int randWordLenMax = 15;
        int relatedMsgCntMin = 2;
        int relatedMsgCntMax = 8;
        ScheduledExecutorService executor = newSingleThreadScheduledExecutor();
        System.out.println("generating log messages...");
        for (int i = 0; i < limit; i++) {

            LogMessage.Level randomLevel = LogMessage.Level.getRandomLevel(random);
            final String trackingId = UUID.randomUUID().toString();

            if (randomLevel.equals(LogMessage.Level.error) || randomLevel.equals(LogMessage.Level.fatal)) {

                // generate other log messages related to the error with the same trackingId
                int r = random.nextInt((relatedMsgCntMax - relatedMsgCntMin) + 1) + relatedMsgCntMin;
                for (int j = 0; j < r; j++) {
                    do {
                        randomLevel = LogMessage.Level.getRandomLevel(random);
                    } while (randomLevel.equals(LogMessage.Level.error) || randomLevel.equals(LogMessage.Level.fatal));
                    new LogMessage.Builder(LOGGER, randomLevel, lorem.getWords(randWordLenMin, randWordLenMax))
                            .addTag("trackId", trackingId)
                            .addTag("identifier", randomLevel.toString())
                            .log();
                }

                // log the error itself last a little later so it appears after all the related log messages
                executor.schedule(() -> {
                    new LogMessage.Builder(LOGGER, LogMessage.Level.error, lorem.getWords(randWordLenMin, randWordLenMax))
                            .addTag("trackId", trackingId)
                            .addTag("identifier", LogMessage.Level.error.toString())
                            .log();
                }, 1500, MILLISECONDS);

            } else {

                // log the non error normally
                new LogMessage.Builder(LOGGER, randomLevel, lorem.getWords(randWordLenMin, randWordLenMax))
                        .addTag("trackId", trackingId)
                        .addTag("identifier", randomLevel.toString())
                        .log();

            }
        }
        System.out.println("done");

        executor.shutdown();

    }

    static private String hexEncode(byte[] input) {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        for (int idx = 0; idx < input.length; ++idx) {
            byte b = input[idx];
            result.append(digits[(b & 0xf0) >> 4]);
            result.append(digits[b & 0x0f]);
        }
        return result.toString();
    }

}
