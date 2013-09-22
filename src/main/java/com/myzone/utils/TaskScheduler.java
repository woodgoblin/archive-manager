package com.myzone.utils;

import java.util.concurrent.*;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * @author myzone
 * @date 9/22/13 1:02 PM
 */
public class TaskScheduler {

    protected static final ScheduledExecutorService EXECUTOR_SERVICE = newScheduledThreadPool(1);

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return EXECUTOR_SERVICE.schedule(command, delay, unit);
    }

    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return EXECUTOR_SERVICE.schedule(callable, delay, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return EXECUTOR_SERVICE.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return EXECUTOR_SERVICE.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

}
