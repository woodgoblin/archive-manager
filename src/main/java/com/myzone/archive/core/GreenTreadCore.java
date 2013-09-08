package com.myzone.archive.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author myzone
 * @date 9/8/13 11:42 AM
 */
public abstract class GreenTreadCore<N> implements Core<N> {

    private final Map<Service<?, ?>, Function<Runnable, Void>> executeOnFunctions;
    private final List<ExecutorService> executorsMap;

    private final AtomicInteger serviceLoadCounter;

    protected GreenTreadCore() {
        executeOnFunctions = new ConcurrentHashMap<>();
        executorsMap = copyOf(createExecutorsMap());
        serviceLoadCounter = new AtomicInteger(0);
    }

    @Override
    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        Function<Runnable, Void> executeOn = executeOnFunctions.getOrDefault(service, (arg) -> {
            throw new RuntimeException("Unknown service " + service);
        });

        Function<R, Void> finalCallback = isUiThread() ? toUiCallback(callback) : callback;

        if (service instanceof PureService) {
            PureService pureService = (PureService) service;

            executeOn.apply(() -> pureService.process(request, finalCallback));
        } else if (service instanceof DataService) {
            DataService dataService = (DataService) service;

            executeOn.apply(() -> dataService.process(request, finalCallback, getDataContext()));
        }
    }

    @Override
    public void loadActivity(@NotNull Activity<? extends N> activity) {
        runOnUiThread(() -> activity.onLoad(getGraphicsContext()));
    }

    @Override
    public void unloadActivity(@NotNull Activity<? extends N> activity) {
        runOnUiThread(() -> activity.onUnload(getGraphicsContext()));
    }

    @Override
    public void loadService(@NotNull DataService<?, ?> service) {
        int i = serviceLoadCounter.incrementAndGet();
        ExecutorService executor = service instanceof PureService ? executorsMap.get(i % executorsMap.size()) : newSingleThreadExecutor();

        executeOnFunctions.putIfAbsent(service, runnable -> {
            executor.submit(runnable);

            return null;
        });

        service.onLoad(this);
    }

    @Override
    public void unloadService(@NotNull DataService<?, ?> service) {
        executeOnFunctions.remove(service);

        service.onUnload(this);
    }

    @NotNull
    protected abstract ApplicationGraphicsContext<N> getGraphicsContext();

    @NotNull
    protected abstract ApplicationDataContext getDataContext();

    protected abstract boolean isUiThread();

    protected abstract void runOnUiThread(@NotNull Runnable runnable);

    @NotNull
    protected ArrayList<ExecutorService> createExecutorsMap() {
        int cores = Runtime.getRuntime().availableProcessors();
        ArrayList<ExecutorService> result = new ArrayList<>(cores);

        for (int i = 0; i < cores; i++) {
            result.add(newSingleThreadExecutor());
        }

        return result;
    }

    @NotNull
    protected <T> Function<T, Void> toUiCallback(@NotNull Function<T, Void> callback) {
        return arg -> {
            runOnUiThread(() -> {
                callback.apply(arg);
            });

            return null;
        };
    }

}
