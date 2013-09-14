package com.myzone.archivemanager.core;

import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.utils.ImmutableTuple;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:47 AM
 */
public interface Core<N, D extends Core.DataProvider> {

    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback);

    public void loadActivity(@NotNull Activity<? extends N> activity);

    public void unloadActivity(@NotNull Activity<? extends N> activity);

    public <A, R, S> void loadService(@NotNull ProcessingService<A, R, S> service);

    public <A, R, S> void unloadService(@NotNull ProcessingService<A, R, S> service);

    public <A, R> void loadService(@NotNull DataService<A, R, ? super D> service);

    public <A, R> void unloadService(@NotNull DataService<A, R, ? super D> service);

    public interface ApplicationGraphicsContext<N> {

        void bind(@NotNull N node);

        void unbind(@NotNull N node);

    }

    public interface ApplicationDataContext<D extends DataProvider> {

        @NotNull
        D getDataProvider();

    }

    public interface ApplicationProcessingContext<A, R, S> {

        S getState();

        void setState(S state);

        void yield(A request, @NotNull Function<? super R, Void> callback) throws ProcessingService.YieldException;

    }

    interface DataProvider<D, T extends DataProvider> extends ImmutableTuple<DataAccessor<D>, T> {

        enum DataProviderEnd implements DataProvider<Object, DataProviderEnd> {

            END;

            @Override
            public DataAccessor<Object> get() {
                return null;
            }

            @NotNull
            @Override
            public DataProviderEnd next() {
                return this;
            }

        }

    }

}
