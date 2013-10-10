package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 9/6/13 10:21 AM
 */
public interface DataService<A, R, D> extends Service<A, R> {

    void process(A request, @NotNull Consumer<? super R> callback, @NotNull Core.ApplicationDataContext<? extends D> dataContext);

    void onLoad(@NotNull Core<?, ? extends D> core);

    void onUnload(@NotNull Core<?, ? extends D> core);

}
