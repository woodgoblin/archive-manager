package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/14/13 7:46 PM
 */
public interface ScheduledCore<N, D extends Core.DataProvider> extends Core<N, D> {

    interface UiBinding<N> {

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

    interface GraphicsContextProvider<N> {

        @NotNull
        ApplicationGraphicsContext<N> provide(@NotNull Activity<? extends N> activity);

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
        public abstract void yield(A request, @NotNull Function<? super R, Void> callback) throws ProcessingService.YieldException;

    }

}
