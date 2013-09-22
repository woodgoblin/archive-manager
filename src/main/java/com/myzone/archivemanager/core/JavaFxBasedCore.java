package com.myzone.archivemanager.core;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 11:51 AM
 */
public class JavaFxBasedCore<D extends Core.DataProvider> implements Core<Node, D> {

    private final ScheduledCore<Node, D> underlyingCore;

    public JavaFxBasedCore(
            @NotNull ScheduledCore.Factory<Node, D> underlyingCoreFactory,
            @NotNull ScheduledCore.DataContextProvider<D> dataContextProvider
    ) {
        underlyingCore = underlyingCoreFactory.create(
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
                new ScheduledCore.GraphicsContextProvider<Node>() {

                    @NotNull
                    @Override
                    public <R> ApplicationGraphicsContext<Node> provide(@NotNull Activity<? extends Node> activity, @NotNull R root, @NotNull Binder<? super R, ? super Node> binder) {
                        return new ApplicationGraphicsContext<Node>() {

                            @Override
                            public void bind(@NotNull Node node) {
                                binder.bind(root, node);
                            }

                            @Override
                            public void unbind(@NotNull Node node) {
                                binder.unbind(root, node);
                            }

                        };
                    }
                },
                dataContextProvider
        );
    }

    @Override
    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
        underlyingCore.processRequest(service, request, callback);
    }

    @Override
    public <R> void loadActivity(@NotNull Activity<? extends Node> activity, @NotNull R root, @NotNull Binder<? super R, ? super Node> binder) {
        underlyingCore.loadActivity(activity, root, binder);
    }

    @Override
    public <R> void unloadActivity(@NotNull Activity<? extends Node> activity, @NotNull R root, @NotNull Binder<? super R, ? super Node> binder) {
        underlyingCore.unloadActivity(activity, root, binder);
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
    public <A, R> void loadService(@NotNull DataService<A, R, ? super D> service) {
        underlyingCore.loadService(service);
    }

    @Override
    public <A, R> void unloadService(@NotNull DataService<A, R, ? super D> service) {
        underlyingCore.unloadService(service);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Pane, N extends Node> Binder<P, N> binder() {
        return (Binder<P, N>) JavaFxBinder.BINDER;
    }

    protected static enum JavaFxBinder implements Core.Binder<Pane, Node> {

        BINDER;

        @Override
        public void bind(@NotNull Pane root, @NotNull Node node) {
            root.getChildren().add(node);
        }

        @Override
        public void unbind(@NotNull Pane root, @NotNull Node node) {
            root.getChildren().remove(node);
        }

    }

}
