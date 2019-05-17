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
public class LogMessageGenerator {

    // each class must declare it's own logger and pass it to the LogBuilder or else we lose class level log scope
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LogMessageGenerator.class.getName());

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
            System.err.println("Usage LogMessageGenerator <message-limit> <message-rate> <error-rate-limit> <error-delay>\n"
                    + "message-limit - the number of messages to generate (-1 to run continously)\n"
                    + "message-rate - the rate per second to generate messages\n"
                    + "error-rate-limit - the max pct of errors to generate relative to overall messages generated\n"
                    + "error-delay - the number of milliseconds to delay the error message from being generated\n"
                    + "\n");
            System.exit(1);
        }
        Lorem lorem = LoremIpsum.getInstance(); // random content of the messages will be based on Lorem Ipsum
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
        for (int i = 0; i < messageLimit; i++) { // stay within the bounds of the runtime message limit defined by startup paramater 

            final String trackingId = UUID.randomUUID().toString(); // a UUID will become the message tracking id to be able to correlate related messages 
            if (usedTrackingIds.contains(trackingId)) { // this check may be unnecessary but... 
                throw new RuntimeException("Unexpected Condition. Tracking Id must be unique. Found more than one generated Tradking Id for trackingId: " + trackingId);
            }
            usedTrackingIds.add(trackingId); // just keep track of them anyways 

            LogMessage.Level randomLevel = LogMessage.Level.getRandomLevel(random); // select one of the log message levels 

            if (randomLevel.equals(LogMessage.Level.error)) { // if it is an error, then we need to generate some related messages tied together with that tracking id 
                double errRate = (i > 0) ? (float) (errCnt) / (float) (i) : 0.0;  // calculate the current rate of errors and don't divide by zero the first time
                if (errRate <= errRateLimit) { // stay within the specified error rate 
                    int r = random.nextInt((relatedMsgCntMax - relatedMsgCntMin) + 1) + relatedMsgCntMin;
                    for (int j = 0; j < r; j++) {
                        do {
                            randomLevel = LogMessage.Level.getRandomLevel(random);
                        } while (randomLevel.equals(LogMessage.Level.error)); // now randomly generate some log levels other than error 
                        if (randomLevel.equals(LogMessage.Level.error)) {
                            throw new RuntimeException("Unexpected Condition. This should never be " + LogMessage.Level.error.toString());
                        }
                        String randomLevelAsString = randomLevel.toString();
                        rateLimiter.execute(() -> { // rateLimiter will emit log messages at the rate specified at startup 
                            new LogMessage.Builder(LOGGER, LogMessage.Level.valueOf(randomLevelAsString), lorem.getWords(randWordLenMin, randWordLenMax))
                                    .addTag("trackId", trackingId)
                                    .addTag("level", randomLevelAsString)
                                    .log();
                        });
                        i++;
                        Integer get = counts.get(randomLevelAsString); // keeping track of counts by log level for display and verification purposes 
                        counts.put(randomLevelAsString, ((get != null) ? get : 0) + 1);
                    }
                    rateLimiter.execute(() -> { // once the related messages related to the error are emitted emit the error itself but late so it is downstream of the related messsages 
                        try {
                            TimeUnit.MILLISECONDS.sleep(errorDelay); // delay emission for a bit 
                            new LogMessage.Builder(LOGGER, LogMessage.Level.error, lorem.getWords(randWordLenMin, randWordLenMax))
                                    .addTag("trackId", trackingId)
                                    .addTag("level", LogMessage.Level.error.toString())
                                    .log();
                        } catch (InterruptedException ex) {
                        }
                    });
                    errCnt++; // keep track of errors to keep track of current rate limit 
                    Integer get = counts.get(LogMessage.Level.error.toString());
                    counts.put(LogMessage.Level.error.toString(), ((get != null) ? get : 0) + 1);
                } else {
                    i--; // don't count this iteration as nothing got logged, the error rate limit was exceeded 
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
