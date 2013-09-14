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
            @NotNull Pane rootNode,
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
                dataContextProvider
        );
    }

    @Override
    public <A, R> void processRequest(@NotNull Service<? super A, ? extends R> service, A request, @NotNull Function<R, Void> callback) {
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
    public <A, R> void loadService(@NotNull DataService<A, R, ? super D> service) {
        underlyingCore.loadService(service);
    }

    @Override
    public <A, R> void unloadService(@NotNull DataService<A, R, ? super D> service) {
        underlyingCore.unloadService(service);
    }


}
