package com.myzone.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 10/14/13 11:35 AM
 */
public class SynchronizedConsumer<T> implements Consumer<T> {

    private final CountDownLatch latch;
    private volatile ResultHolder<T> resultHolder;

    public SynchronizedConsumer() {
        latch = new CountDownLatch(1);
    }

    @Override
    public void accept(T arg) {
        resultHolder = new ResultHolder<>(arg);

        latch.countDown();
    }

    public T get() {
        if (resultHolder == null) {
            boolean done = false;

            while (!done) {
                try {
                    latch.await();

                    done = true;
                } catch (InterruptedException e) {
                    // try once more
                }
            }
        }

        return resultHolder.getResult();
    }

    public T getInterruptably() throws InterruptedException {
        if (resultHolder == null) {
            latch.await();
        }

        return resultHolder.getResult();
    }


    public T getInterruptably(long timeout, @NotNull TimeUnit timeUnit) throws InterruptedException {
        if (resultHolder == null) {
            latch.await(timeout, timeUnit);
        }

        return resultHolder.getResult();
    }

    private static class ResultHolder<T> {

        private final T result;

        private ResultHolder(T result) {
            this.result = result;
        }

        private T getResult() {
            return result;
        }

    }

}
