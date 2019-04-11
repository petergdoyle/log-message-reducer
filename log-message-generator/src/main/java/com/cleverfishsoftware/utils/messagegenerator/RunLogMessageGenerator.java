/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import org.apache.logging.log4j.LogManager;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
public class RunLogMessageGenerator {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        int limit = 0;
        float rateLimit = 0.0f;
        boolean error = false;
        if (args == null || args.length == 0) {
            error = true;
        } else {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                error = true;
            }
            try {
                rateLimit = Float.parseFloat(args[1]);
            } catch (Exception ex) {
                error = true;
            }

        }
        if (error) {
            System.err.println("Usage RunLogMessageGenerator <size> <rate>\n"
                    + "size - the number of messages to generate\n"
                    + "rate - the rate per second to generate messages\n\n");
            System.exit(1);
        }
        // each class must declare it's own logger and pass it to the LogBuilder or else we lose class level log scope
        org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RunLogMessageGenerator.class.getName());
        Lorem lorem = LoremIpsum.getInstance();
        Random random = new Random();
        int randWordLenMin = 5;
        int randWordLenMax = 15;
        int relatedMsgCntMin = 2;
        int relatedMsgCntMax = 8;
        LogMessageRateLimiter rateLimiter = new LogMessageRateLimiter(rateLimit);
        float seconds = limit / rateLimit;
        System.out.printf("generating log messages (should take aproximately %.1f seconds to complete)...\n", seconds);
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
                    String randomLevelAsString = randomLevel.toString();
                    rateLimiter.execute(() -> {
                        new LogMessage.Builder(LOGGER, LogMessage.Level.valueOf(randomLevelAsString), lorem.getWords(randWordLenMin, randWordLenMax))
                                .addTag("trackId", trackingId)
                                .addTag("identifier", randomLevelAsString)
                                .log();
                    });
                }

                // log the error itself last a little later so it appears after all the related log messages
                rateLimiter.execute(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1500);
                        new LogMessage.Builder(LOGGER, LogMessage.Level.error, lorem.getWords(randWordLenMin, randWordLenMax))
                                .addTag("trackId", trackingId)
                                .addTag("identifier", LogMessage.Level.error.toString())
                                .log();
                    } catch (InterruptedException ex) {
                    }
                });

            } else {

                // log the non error normally using the RateLimiter
                String randomLevelAsString = randomLevel.toString();
                rateLimiter.execute(() -> {
                    new LogMessage.Builder(LOGGER, LogMessage.Level.valueOf(randomLevelAsString), lorem.getWords(randWordLenMin, randWordLenMax))
                            .addTag("trackId", trackingId)
                            .addTag("identifier", randomLevelAsString)
                            .log();
                });

            }
        }

        rateLimiter.shutdown();

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
