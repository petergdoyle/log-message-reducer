package com.cleverfishsoftware.utils.messagegenerator;

import com.google.common.util.concurrent.RateLimiter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogMessageRateLimiter {

    private final RateLimiter limiter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public LogMessageRateLimiter(final Float rate) {
        limiter = RateLimiter.create(rate);
    }

    public static void main(String[] args) {
        LogMessageRateLimiter logMessageRateLimiter = new LogMessageRateLimiter(20.0f);
        for (int i = 0; i < 100; i++) {
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
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

}
