package com.cleverfishsoftware.utils.messagegenerator;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// https://stackoverflow.com/questions/46845352/google-ratelimiter-not-working-for-counter
// https://stackoverflow.com/questions/19819837/java-executor-with-throttling-throughput-control

public class LogMessageRateLimiter {

    private final RateLimiter limiter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    // private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LogMessageRateLimiter(final Float rate) {
        limiter = RateLimiter.create(rate);
    }

    public static void main(String[] args) {

        int limit = 0;
        float rate = 0.0f;
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
              rate = Float.parseFloat(args[1]);
          } catch (Exception ex) {
              error = true;
          }

        }
        if (error) {
          System.err.println("Usage LogMessageRateLimiter <limit> <rate>\n"
                  + "limit - the number of messages to generate\n"
                  + "rate - the rate per second to generate messages\n\n");
          System.exit(1);
        }

        LogMessageRateLimiter logMessageRateLimiter = new LogMessageRateLimiter(rate);
        for (int i = 0; i < limit; i++) {
            logMessageRateLimiter.execute(() -> {
                System.out.println(new Date() + ": Beep");
            });
        }
        logMessageRateLimiter.shutdown();

    }

    public void execute(Runnable task) {
        limiter.acquire();
        executorService.execute(task);
    }

    public void shutdown() {
        executorService.shutdown();
//        try {
//            if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
//                executorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executorService.shutdownNow();
//        }
    }

}
