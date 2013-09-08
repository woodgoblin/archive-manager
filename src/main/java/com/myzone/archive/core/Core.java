package com.myzone.archive.core;

import com.myzone.archive.data.DataAccessor;
import com.myzone.archive.model.Document;
import com.myzone.archive.model.User;
import com.myzone.utils.ImmutableTuple;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:47 AM
 */
public interface Core<N> {

    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback);

    public void loadActivity(@NotNull Activity<? extends N> activity);

    public void unloadActivity(@NotNull Activity<? extends N> activity);

    public void loadService(@NotNull DataService<?, ?> service);

    public void unloadService(@NotNull DataService<?, ?> service);

    public interface ApplicationGraphicsContext<N> {

        void bind(@NotNull N node);

        void unbind(@NotNull N node);

    }

    public interface ApplicationDataContext {

        @NotNull
        DataAccessor<User> getUserDataAccessor();

        @NotNull
        DataAccessor<Document> getDocumentDataAccessor();

    }

}
