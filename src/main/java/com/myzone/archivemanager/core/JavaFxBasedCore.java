package com.myzone.archivemanager.core;

import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.data.InMemoryDataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.utils.RecursiveImmutableTuple;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:51 AM
 */
public class JavaFxBasedCore implements Core<Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> {

    private final GreenTreadCore<Node, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> underlyingCore;

    public JavaFxBasedCore(@NotNull Pane rootNode) {
        underlyingCore = new GreenTreadCore<>(
                new GreenTreadCore.UiBinding<Node>() {

                    @Override
                    public boolean isUiThread() {
                        return Platform.isFxApplicationThread();
                    }

                    @Override
                    public void runOnUiThread(@NotNull Runnable runnable) {
                        Platform.runLater(runnable);
                    }

                },
                new GreenTreadCore.GraphicsContextProvider<Node>() {

                    @NotNull
                    @Override
                    public ApplicationGraphicsContext<Node> provide(@NotNull Activity<? extends Node> activity) {
                        return new ApplicationGraphicsContext<Node>() {

                            @Override
                            public void bind(@NotNull Node node) {
                                rootNode.getChildren().add(node);
                            }

                            @Override
                            public void unbind(@NotNull Node node) {
                                rootNode.getChildren().remove(node);
                            }

                        };
                    }

                },
                new GreenTreadCore.DataContextProvider<DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>>() {

                    @NotNull
                    @Override
                    public <A, R> ApplicationDataContext<? extends DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> provide(@NotNull DataService<A, R, ? super DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> dataService) {
                        return new ApplicationDataContext<DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>>() {

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

                },
                new GreenTreadCore.ProcessingContextProvider<Object>() {

                    @NotNull
                    @Override
                    public <A, R, S> ApplicationProcessingContext<A, R, S> provide(@NotNull ProcessingService<A, R, S> processingService) {
                        return new GreenTreadCore.GreenThreadProcessingContext<A, R, S>(underlyingCore, processingService) {

                            private S data = null;

                            @Override
                            public S getState() {
                                return data;
                            }

                            @Override
                            public void setState(S state) {
                                this.data = state;
                            }

                        };
                    }

                }
        );
    }

    @Override
    public <A, R, S> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        underlyingCore.processRequest(service, request, callback);
    }

    @Override
    public void loadActivity(@NotNull Activity<? extends Node> activity) {
        underlyingCore.loadActivity(activity);
    }

    @Override
    public void unloadActivity(@NotNull Activity<? extends Node> activity) {
        underlyingCore.unloadActivity(activity);
    }

    @Override
    public <A, R, S> void loadService(@NotNull ProcessingService<A, R, S> service) {
        underlyingCore.loadService(service);
    }

    @Override
    public <A, R, S> void unloadService(@NotNull ProcessingService<A, R, S> service) {
        underlyingCore.unloadService(service);
    }

    @Override
    public <A, R> void loadService(@NotNull DataService<A, R, ? super DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> service) {
        underlyingCore.loadService(service);
    }

    @Override
    public <A, R> void unloadService(@NotNull DataService<A, R, ? super DataProvider<User, DataProvider<Document, DataProvider.DataProviderEnd>>> service) {
        underlyingCore.unloadService(service);
    }

    protected static class RecursiveDataProvider<D, T extends Core.DataProvider> extends RecursiveImmutableTuple<DataAccessor<D>, T> implements Core.DataProvider<D, T> {

        public RecursiveDataProvider(DataAccessor<D> data, T next) {
            super(data, next);
        }

    }

}
