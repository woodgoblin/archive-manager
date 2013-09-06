package com.myzone.archive.core;

import com.myzone.archive.data.DataAccessor;
import com.myzone.archive.model.Document;
import com.myzone.archive.model.User;

/**
 * @author myzone
 * @date 9/6/13 11:47 AM
 */
public interface Core<N> {

    public <A, R> R processRequest(Service<? super A, ? extends R> service, A request);

    public void loadActivity(Activity<? extends N> activity);

    public void unloadActivity(Activity<? extends N> activity);

    public void loadService(Service<?, ?> service);

    public void unloadService(Service<?, ?> service);

    public interface ApplicationGraphicsContext<N> {

        void bind(N node);

        void unbind(N node);

    }

    public interface ApplicationDataContext {

        DataAccessor<User> getUserDataAccessor();

        DataAccessor<Document> getDocumentDataAccessor();

    }

}
