package com.myzone.archive.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author myzone
 * @date 9/8/13 11:42 AM
 */
public abstract class GreenTreadCore<N, D extends Core.Type> implements Core<N, D> {

    private final Map<Service<?, ?>, Function<Runnable, Void>> executeOnFunctions;
    private final ExecutorService executor;

    protected GreenTreadCore() {
        executeOnFunctions = new ConcurrentHashMap<>();
        executor = new ForkJoinPool();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        Function<Runnable, Void> executeOn = executeOnFunctions.getOrDefault(service, (arg) -> {
            throw new RuntimeException("Unknown service " + service);
        });

        Function<R, Void> finalCallback = isUiThread() ? toUiCallback(callback) : callback;

        if (service instanceof PureService) {
            PureService<? super A, ? extends R> pureService = (PureService<? super A, ? extends R>) service;

            executeOn.apply(() -> pureService.process(request, finalCallback));
        } else if (service instanceof DataService) {
            DataService<? super A, ? extends R, ? super D> dataService = (DataService<? super A, ? extends R, ? super D>) service;

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
    public void loadService(@NotNull PureService<?, ?> service) {
        executeOnFunctions.putIfAbsent(service, runnable -> {
            executor.submit(runnable);

            return null;
        });

        service.onLoad(this);
    }

    @Override
    public void unloadService(@NotNull PureService<?, ?> service) {
        executeOnFunctions.remove(service);

        service.onUnload(this);
    }

    @Override
    public void loadService(@NotNull DataService<?, ?, ? super D> service) {
        ExecutorService privateExecutor = newSingleThreadExecutor();

        executeOnFunctions.put(service, runnable -> {
            privateExecutor.submit(runnable);

            return null;
        });

        service.onLoad(this);
    }

    @Override
    public void unloadService(@NotNull DataService<?, ?, ? super D> service) {
        executeOnFunctions.remove(service);

        service.onUnload(this);
    }

    @NotNull
    protected abstract ApplicationGraphicsContext<? super N> getGraphicsContext();

    @NotNull
    protected abstract ApplicationDataContext<? extends D> getDataContext();

    protected abstract boolean isUiThread();

    protected abstract void runOnUiThread(@NotNull Runnable runnable);

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
