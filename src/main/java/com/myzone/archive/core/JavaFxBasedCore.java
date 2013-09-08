package com.myzone.archive.core;

import com.myzone.archive.data.DataAccessor;
import com.myzone.archive.model.Document;
import com.myzone.archive.model.User;
import com.myzone.utils.ImmutableTuple;
import com.myzone.utils.RecursiveImmutableTuple;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static javafx.application.Platform.isFxApplicationThread;
import static javafx.application.Platform.runLater;

/**
 * @author myzone
 * @date 9/6/13 11:51 AM
 */
public class JavaFxBasedCore extends GreenTreadCore<Node, Core.Type<User, Core.Type<Document, Core.Type.End>>> {

    private final ApplicationGraphicsContext<Node> applicationGraphicsContext;
    private final ApplicationDataContext<Core.Type<User, Core.Type<Document, Core.Type.End>>> applicationDataContext;

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
        applicationDataContext = new ApplicationDataContext<Type<User, Type<Document, Type.End>>>() {

            private Set<User> users = new HashSet<>();

            @NotNull
            @Override
            public Type<User, Type<Document, Type.End>> getDataAccessor() {
                return new RecursiveType<User, Type<Document, Type.End>>(
                        () -> new DataAccessor.Transaction<User>() {

                            Set<User> tmp = new HashSet<>(users);

                            @Override
                            public Stream<User> getAll() throws DataAccessor.DataAccessException {
                                return tmp.stream();
                            }

                            @Override
                            public void save(User o) throws DataAccessor.DataAccessException {
                                tmp.add(o);
                            }

                            @Override
                            public void update(User o) throws DataAccessor.DataAccessException {
                            }

                            @Override
                            public void delete(User o) throws DataAccessor.DataAccessException {
                                tmp.remove(o);
                            }

                            @Override
                            public void commit() throws DataAccessor.DataModificationException {
                                users = tmp;
                            }

                            @Override
                            public void rollback() {
                            }
                        },
                        new RecursiveType<>(
                                () -> {
                                    throw new UnsupportedOperationException();
                                },
                                Type.End.END
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
    protected ApplicationDataContext getDataContext() {
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

    class RecursiveType<D, T extends Type> extends RecursiveImmutableTuple<DataAccessor<D>, T> implements Type<D, T> {

        public RecursiveType(DataAccessor<D> data, T next) {
            super(data, next);
        }

    }

}
