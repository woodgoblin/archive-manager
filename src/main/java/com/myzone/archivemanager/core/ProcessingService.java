package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/8/13 3:14 PM
 */
public interface ProcessingService<A, R, S> extends Service<A, R> {

    void process(A request, @NotNull Function<? super R, Void> callback, @NotNull Core.ApplicationProcessingContext<? super A, ? extends R, S> processingContext) throws YieldException;

    void onLoad(@NotNull Core<?, ?> core);

    void onUnload(@NotNull Core<?, ?> core);

    class YieldException extends RuntimeException {

    }

}
