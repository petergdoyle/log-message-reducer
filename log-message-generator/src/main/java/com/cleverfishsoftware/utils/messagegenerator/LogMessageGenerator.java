/*
 */
package com.cleverfishsoftware.utils.messagegenerator;

import org.apache.logging.log4j.LogManager;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 */
public class RunLogMessageGenerator {

    // each class must declare it's own logger and pass it to the LogBuilder or else we lose class level log scope
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RunLogMessageGenerator.class.getName());

    public static void main(String[] args) throws NoSuchAlgorithmException {
        int messageLimit = 0;
        float rateLimit = 0.0f;
        float errRateLimit = 0.f;
        int errorDelayValue = 0;
        boolean error = false;
        if (args == null || args.length == 0 || args.length < 4) {
            error = true;
        } else {
            try {
                messageLimit = Integer.parseInt(args[0]);
                if (messageLimit < 1) {
                    // assume run continously
                    messageLimit = Integer.MAX_VALUE;
                }
            } catch (Exception ex) {
                error = true;
            }
            try {
                rateLimit = Float.parseFloat(args[1]);
            } catch (Exception ex) {
                error = true;
            }
            try {
                errRateLimit = Float.parseFloat(args[2]);
            } catch (Exception ex) {
                error = true;
            }
            try {
                errorDelayValue = Integer.parseInt(args[3]);
            } catch (Exception ex) {
                error = true;
            }

        }
        if (error) {
            System.err.println("Usage RunLogMessageGenerator <message-limit> <message-rate> <error-rate-limit> <error-delay>\n"
                    + "message-limit - the number of messages to generate (-1 to run continously)\n"
                    + "message-rate - the rate per second to generate messages\n"
                    + "error-rate-limit - the max pct of errors to generate relative to overall messages generated\n"
                    + "error-delay - the number of milliseconds to delay the error message from being generated\n"
                    + "\n");
            System.exit(1);
        }
        Lorem lorem = LoremIpsum.getInstance();
        Random random = new Random();
        int randWordLenMin = 5;
        int randWordLenMax = 15;
        int relatedMsgCntMin = 2;
        int relatedMsgCntMax = 8;
        int errCnt = 0;
        final Integer errorDelay = errorDelayValue;
        LogMessageRateLimiter rateLimiter = new LogMessageRateLimiter(rateLimit);
        float seconds = messageLimit / rateLimit;
        Set<String> usedTrackingIds = new HashSet<>();
        Map<String, Integer> counts = new HashMap<>();
        System.out.printf("\n\n[INFO] generating %d log messages "
                + "throttled at a rate of %.0f per second, "
                + "with an error-rate of %.2f pct "
                + "and an error delay of %d milliseconds. "
                + "it should take aproximately %.1f "
                + "seconds to complete...\n\n", messageLimit, rateLimit, errRateLimit, errorDelay, seconds);
        for (int i = 0; i < messageLimit; i++) {

            final String trackingId = UUID.randomUUID().toString();
            if (usedTrackingIds.contains(trackingId)) {
                throw new RuntimeException("Unexpected Condition. Tracking Id must be unique. Found more than one generated Tradking Id for trackingId: " + trackingId);
            }
            usedTrackingIds.add(trackingId);

            LogMessage.Level randomLevel = LogMessage.Level.getRandomLevel(random);

            if (randomLevel.equals(LogMessage.Level.error)) {
                double errRate = (i > 0) ? (float) (errCnt) / (float) (i) : 0.0;
                if (errRate <= errRateLimit) {
                    int r = random.nextInt((relatedMsgCntMax - relatedMsgCntMin) + 1) + relatedMsgCntMin;
                    for (int j = 0; j < r; j++) {
                        do {
                            randomLevel = LogMessage.Level.getRandomLevel(random);
                        } while (randomLevel.equals(LogMessage.Level.error));
                        if (randomLevel.equals(LogMessage.Level.error)) {
                            throw new RuntimeException("Unexpected Condition. This should never be " + LogMessage.Level.error.toString());
                        }
                        String randomLevelAsString = randomLevel.toString();
                        rateLimiter.execute(() -> {
                            new LogMessage.Builder(LOGGER, LogMessage.Level.valueOf(randomLevelAsString), lorem.getWords(randWordLenMin, randWordLenMax))
                                    .addTag("trackId", trackingId)
                                    .addTag("level", randomLevelAsString)
                                    .log();
                        });
                        i++;
                        Integer get = counts.get(randomLevelAsString);
                        counts.put(randomLevelAsString, ((get != null) ? get : 0) + 1);
                    }
                    rateLimiter.execute(() -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(errorDelay);
                            new LogMessage.Builder(LOGGER, LogMessage.Level.error, lorem.getWords(randWordLenMin, randWordLenMax))
                                    .addTag("trackId", trackingId)
                                    .addTag("level", LogMessage.Level.error.toString())
                                    .log();
                        } catch (InterruptedException ex) {
                        }
                    });
                    errCnt++;
                    Integer get = counts.get(LogMessage.Level.error.toString());
                    counts.put(LogMessage.Level.error.toString(), ((get != null) ? get : 0) + 1);
                } else {
                    i--; // don't count this iteration as nothing got logged
                }
            } else {
                // log the non error normally using the RateLimiter
                String randomLevelAsString = randomLevel.toString();
                rateLimiter.execute(() -> {
                    new LogMessage.Builder(LOGGER, LogMessage.Level.valueOf(randomLevelAsString), lorem.getWords(randWordLenMin, randWordLenMax))
                            .addTag("trackId", trackingId)
                            .addTag("level", randomLevelAsString)
                            .log();
                });
                Integer get = counts.get(randomLevelAsString);
                counts.put(randomLevelAsString, ((get != null) ? get : 0) + 1);
            }

            System.out.printf("\r[LogMessageGenerator] Total: %d %s", i + 1, counts);
            if (messageLimit + 1 > Integer.MAX_VALUE) {
                System.out.println("reached maximum iteration count");
                System.exit(0);
            }
        }

        rateLimiter.shutdown();
        System.out.printf("\n\n");

    }

    static void log(LogMessage.Level level, String msg) {
        switch (level) {
            case trace:
                LOGGER.trace(msg);
                break;
            case debug:
                LOGGER.debug(msg);
                break;
            case warn:
                LOGGER.warn(msg);
                break;
            case info:
                LOGGER.info(msg);
                break;
            case error:
                LOGGER.error(msg);
                break;
            case fatal:
                LOGGER.fatal(msg);
                break;
            default:
                throw new RuntimeException("this isn't supposed to fall thru");
        }
    }

}