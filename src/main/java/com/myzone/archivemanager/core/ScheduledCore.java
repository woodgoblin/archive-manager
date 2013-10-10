package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 9/14/13 7:46 PM
 */
public interface ScheduledCore<N, D extends Core.DataProvider> extends Core<N, D> {

    interface UiBinding<N> {

        boolean isUiThread();

        void runOnUiThread(@NotNull Runnable runnable);

        @NotNull
        default <T> Consumer<T> toUiCallback(@NotNull Consumer<T> callback) {
            return arg -> runOnUiThread(() -> callback.accept(arg));
        }

    }

    interface GraphicsContextProvider<N> {

        @NotNull
        <R> ApplicationGraphicsContext<N> provide(@NotNull Activity<? extends N> activity, @NotNull R root, @NotNull Binder<? super R, ? super N> binder);

    }

    interface DataContextProvider<D extends DataProvider> {

        @NotNull
        <A, R> ApplicationDataContext<? extends D> provide(@NotNull DataService<A, R, ? super D> dataService);

    }

    interface ProcessingContextProvider<S> {

        @NotNull
        <A, R, T extends S> ApplicationProcessingContext<A, R, T> provide(@NotNull ProcessingService<A, R, T> processingService);

    }

    interface Factory<N, D extends DataProvider> {

        @NotNull
        ScheduledCore<N, D> create(
                @NotNull ScheduledCore.UiBinding<? extends N> uiBinding,
                @NotNull ScheduledCore.GraphicsContextProvider<N> graphicsContextProvider,
                @NotNull ScheduledCore.DataContextProvider<D> dataContextProvider
        );

    }

    public interface ScheduledProcessingContext<A, R, S> extends ApplicationProcessingContext<A, R, S> {

        @Override
        public abstract S getState();

        @Override
        public abstract void setState(S state);

        @Override
        public abstract void yield(A request, @NotNull Consumer<? super R> callback) throws ProcessingService.YieldException;

    }

}
