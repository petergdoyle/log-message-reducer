/*
 */
package com.cleverfishsoftware.utils.messagebuillder;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 *
 */
public class DelayExecution {

    public static void main(String[] args) {
        ScheduledExecutorService executor = newSingleThreadScheduledExecutor();

        executor.schedule(() -> {
            System.out.println("i am delayed");
        }, 1, SECONDS);
        executor.shutdown(); 
    }

}
