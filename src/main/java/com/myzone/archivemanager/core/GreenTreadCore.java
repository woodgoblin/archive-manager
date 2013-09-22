package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author myzone
 * @date 9/8/13 11:42 AM
 */
public class GreenTreadCore<N, D extends Core.DataProvider> implements ScheduledCore<N, D> {

    protected final UiBinding<? extends N> uiBinding;

    protected final GraphicsContextProvider<N> graphicsContextProvider;
    protected final DataContextProvider<D> dataContextProvider;
    protected final ProcessingContextProvider<?> processingContextProvider;

    protected final Map<Service<?, ?>, Function<Runnable, Void>> executeOnFunctions;
    protected final ExecutorService executor;


    public GreenTreadCore(
            @NotNull UiBinding<? extends N> uiBinding,
            @NotNull GraphicsContextProvider<N> graphicsContextProvider,
            @NotNull DataContextProvider<D> dataContextProvider
    ) {
        this.uiBinding = uiBinding;
        this.graphicsContextProvider = graphicsContextProvider;
        this.dataContextProvider = dataContextProvider;
        this.processingContextProvider = new ProcessingContextProvider<Object>() {
            @NotNull
            @Override
            public <A, R, T extends Object> ApplicationProcessingContext<A, R, T> provide(@NotNull ProcessingService<A, R, T> processingService) {
                return new GreenThreadProcessingContext<A, R, T>(GreenTreadCore.this, processingService) {

                    private T state = null;

                    @Override
                    public T getState() {
                        return state;
                    }

                    @Override
                    public void setState(T state) {
                        this.state = state;
                    }

                };
            }

        };

        executeOnFunctions = new ConcurrentHashMap<>();
        executor = new ForkJoinPool();
    }

    public GreenTreadCore(
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
    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        Function<Runnable, Void> executeOn = executeOnFunctions.getOrDefault(service, (runnable) -> {
            throw new RuntimeException("Unknown service " + service);
        });

        Function<R, Void> threadSafeCallback = uiBinding.isUiThread() ? uiBinding.toUiCallback(callback) : callback;

        if (service instanceof ProcessingService) {
            ProcessingService processingService = (ProcessingService) service; // todo: it's a dirty hack, should be fixed

            executeOn.apply(() -> {
                try {
                    processingService.process(request, threadSafeCallback, processingContextProvider.provide(processingService));
                } catch (ProcessingService.YieldException ignored) {
                    // just yield has been happened
                }
            });
        } else if (service instanceof DataService) {
            DataService<? super A, ? extends R, ? super D> dataService = (DataService<? super A, ? extends R, ? super D>) service;

            executeOn.apply(() -> dataService.process(request, threadSafeCallback, dataContextProvider.provide(dataService)));
        } else {
            throw new RuntimeException("Unknown service " + service);
        }
    }

    @Override
    public <R> void loadActivity(@NotNull Activity<? extends N> activity, @NotNull R root, @NotNull Binder<? super R, ? super N> binder) {
        uiBinding.runOnUiThread(() -> activity.onLoad(graphicsContextProvider.provide(activity, root, binder)));
    }

    @Override
    public <R> void unloadActivity(@NotNull Activity<? extends N> activity, @NotNull R root, @NotNull Binder<? super R, ? super N> binder) {
        uiBinding.runOnUiThread(() -> activity.onUnload(graphicsContextProvider.provide(activity, root, binder)));
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

    protected static abstract class GreenThreadProcessingContext<A, R, S> implements ScheduledProcessingContext<A, R, S> {

        private final GreenTreadCore<?, ?> greenTreadCore;
        private final ProcessingService<A, R, S> processingService;

        private ProcessingService.YieldException yieldException;

        protected GreenThreadProcessingContext(
                @NotNull GreenTreadCore<?, ?> greenTreadCore,
                @NotNull ProcessingService<A, R, S> processingService
        ) {
            this.greenTreadCore = greenTreadCore;
            this.processingService = processingService;
        }

        @Override
        public void yield(A request, @NotNull Function<? super R, Void> callback) throws ProcessingService.YieldException {
            greenTreadCore.executor.submit(() -> processingService.process(request, callback, this));

            if (yieldException == null) {
                yieldException = new ProcessingService.YieldException();
            }

            throw yieldException;
        }

    }

}
