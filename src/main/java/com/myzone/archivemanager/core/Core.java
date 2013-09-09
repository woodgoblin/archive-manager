package com.myzone.archivemanager.core;

import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.utils.ImmutableTuple;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:47 AM
 */
public interface Core<N, D extends Core.Type> {

    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback);

    public void loadActivity(@NotNull Activity<? extends N> activity);

    public void unloadActivity(@NotNull Activity<? extends N> activity);

    public void loadService(@NotNull PureService<?, ?> service);

    public void unloadService(@NotNull PureService<?, ?> service);

    public void loadService(@NotNull DataService<?, ?, ? super D> service);

    public void unloadService(@NotNull DataService<?, ?, ? super D> service);

    public interface ApplicationGraphicsContext<N> {

        void bind(@NotNull N node);

        void unbind(@NotNull N node);

    }

    public interface ApplicationDataContext<D extends Type> {

        @NotNull
        D getDataAccessors();

    }

    public interface Type<D, T extends Type> extends ImmutableTuple<DataAccessor<D>, T> {

        enum End implements Type {

            END;

            @Override
            public Object get() {
                return null;
            }

            @NotNull
            @Override
            public ImmutableTuple next() {
                return this;
            }

        }

    }

}
