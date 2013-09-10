package com.myzone.archivemanager.core;

import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.data.InMemoryDataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.utils.RecursiveImmutableTuple;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static javafx.application.Platform.isFxApplicationThread;
import static javafx.application.Platform.runLater;

/**
 * @author myzone
 * @date 9/6/13 11:51 AM
 */
public class JavaFxBasedCore extends GreenTreadCore<Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> {

    private final ApplicationGraphicsContext<Node> applicationGraphicsContext;
    private final ApplicationDataContext<DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> applicationDataContext;

    public JavaFxBasedCore(@NotNull Pane rootNode) {
        this.applicationGraphicsContext = new ApplicationGraphicsContext<Node>() {

            @Override
            public void bind(@NotNull Node node) {
                rootNode.getChildren().add(node);
            }

            @Override
            public void unbind(@NotNull Node node) {
                rootNode.getChildren().remove(node);
            }

        };
        applicationDataContext = new ApplicationDataContext<DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>>() {

            private Set<User> users = new HashSet<>();

            @NotNull
            @Override
            public DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>> getDataProvider() {
                return new RecursiveDataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>(
                        new InMemoryDataAccessor<>(User.class),
                        new RecursiveDataProvider<>(
                                new InMemoryDataAccessor<>(Document.class),
                                DataProvider.DataProviderEnd.END
                        )
                );
            }
        };
    }

    @NotNull
    @Override
    protected ApplicationGraphicsContext<Node> getGraphicsContext() {
        return applicationGraphicsContext;
    }

    @NotNull
    @Override
    protected ApplicationDataContext<DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> getDataContext() {
        return applicationDataContext;
    }

    @Override
    protected boolean isUiThread() {
        return isFxApplicationThread();
    }

    @Override
    protected void runOnUiThread(@NotNull Runnable runnable) {
        boolean fxApplicationThread = isFxApplicationThread();

        if (fxApplicationThread) {
            runnable.run();
        } else {
            runLater(runnable);
        }
    }

    class RecursiveDataProvider<D, T extends DataProvider> extends RecursiveImmutableTuple<DataAccessor<D>, T> implements DataProvider<D, T> {

        public RecursiveDataProvider(DataAccessor<D> data, T next) {
            super(data, next);
        }

    }

}
