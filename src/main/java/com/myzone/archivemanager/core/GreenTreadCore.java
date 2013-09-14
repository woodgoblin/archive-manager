package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author myzone
 * @date 9/8/13 11:42 AM
 */
public class GreenTreadCore<N, D extends Core.DataProvider> implements Core<N, D> {

    protected final UiBinding<? extends N> uiBinding;

    protected final GraphicsContextProvider<N> graphicsContextProvider;
    protected final DataContextProvider<D> dataContextProvider;
    protected final ProcessingContextProvider<?> processingContextProvider;

    protected final Map<Service<?, ?>, Function<Runnable, Void>> executeOnFunctions;
    protected final ExecutorService executor;

    protected GreenTreadCore(
            @NotNull UiBinding<? extends N> uiBinding,
            @NotNull GraphicsContextProvider<N> graphicsContextProvider,
            @NotNull DataContextProvider<D> dataContextProvider,
            @NotNull ProcessingContextProvider<?> processingContextProvider
    ) {
        this.uiBinding = uiBinding;
        this.graphicsContextProvider = graphicsContextProvider;
        this.dataContextProvider = dataContextProvider;
        this.processingContextProvider = processingContextProvider;

        executeOnFunctions = new ConcurrentHashMap<>();
        executor = new ForkJoinPool();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A, R, S> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        Function<Runnable, Void> executeOn = executeOnFunctions.getOrDefault(service, (runnable) -> {
            throw new RuntimeException("Unknown service " + service);
        });

        Function<R, Void> threadSafeCallback = uiBinding.isUiThread() ? uiBinding.toUiCallback(callback) : callback;

        if (service instanceof ProcessingService) {
            ProcessingService<? super A, ? extends R, S> processingService = (ProcessingService<? super A, ? extends R, S>) service;

            // failure is in line below
            executeOn.apply(() -> processingService.process(request, threadSafeCallback, processingContextProvider.provide(processingService)));
        } else if (service instanceof DataService) {
            DataService<? super A, ? extends R, ? super D> dataService = (DataService<? super A, ? extends R, ? super D>) service;

            executeOn.apply(() -> dataService.process(request, threadSafeCallback, dataContextProvider.provide(dataService)));
        } else {
            throw new RuntimeException("Unknown service " + service);
        }
    }

    public interface ProcessingContextProvider<S> {

        @NotNull
        <A, R, T extends S> ApplicationProcessingContext<A, R, T> provide(@NotNull ProcessingService<A, R, T> processingService);

    }

    @Override
    public void loadActivity(@NotNull Activity<? extends N> activity) {
        uiBinding.runOnUiThread(() -> activity.onLoad(graphicsContextProvider.provide(activity)));
    }

    @Override
    public void unloadActivity(@NotNull Activity<? extends N> activity) {
        uiBinding.runOnUiThread(() -> activity.onUnload(graphicsContextProvider.provide(activity)));
    }

    @Override
    public <A, R, S> void loadService(@NotNull ProcessingService<A, R, S> service) {
        executeOnFunctions.putIfAbsent(service, runnable -> {
            executor.submit(runnable);

            return null;
        });

        service.onLoad(this);
    }

    @Override
    public <A, R, S> void unloadService(@NotNull ProcessingService<A, R, S> service) {
        executeOnFunctions.remove(service);

        service.onUnload(this);
    }

    @Override
    public <A, R> void loadService(@NotNull DataService<A, R, ? super D> service) {
        ExecutorService privateExecutor = newSingleThreadExecutor();

        executeOnFunctions.put(service, runnable -> {
            privateExecutor.submit(runnable);

            return null;
        });

        service.onLoad(this);
    }

    @Override
    public <A, R> void unloadService(@NotNull DataService<A, R, ? super D> service) {
        executeOnFunctions.remove(service);

        service.onUnload(this);
    }

    public interface UiBinding<N> {

        boolean isUiThread();

        void runOnUiThread(@NotNull Runnable runnable);

        @NotNull
        default <T> Function<T, Void> toUiCallback(@NotNull Function<T, Void> callback) {
            return arg -> {
                runOnUiThread(() -> {
                    callback.apply(arg);
                });

                return null;
            };
        }

    }

    public interface GraphicsContextProvider<N> {

        @NotNull
        ApplicationGraphicsContext<N> provide(@NotNull Activity<? extends N> activity);

    }

    public interface DataContextProvider<D extends DataProvider> {

        @NotNull
        <A, R> ApplicationDataContext<? extends D> provide(@NotNull DataService<A, R, ? super D> dataService);

    }

    public static abstract class GreenThreadProcessingContext<A, R, S> implements ApplicationProcessingContext<A, R, S> {

        private final GreenTreadCore<?, ?> greenTreadCore;
        private final ProcessingService<A, R, S> processingService;

        protected GreenThreadProcessingContext(
                @NotNull GreenTreadCore<?, ?> greenTreadCore,
                @NotNull ProcessingService<A, R, S> processingService
        ) {
            this.greenTreadCore = greenTreadCore;
            this.processingService = processingService;
        }

        @Override
        public void yield(A request, @NotNull Function<? super R, Void> callback) {
            greenTreadCore.executor.submit(() -> processingService.process(request, callback, this));
        }

    }

}
