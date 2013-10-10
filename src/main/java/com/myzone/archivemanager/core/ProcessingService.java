package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 9/8/13 3:14 PM
 */
public interface ProcessingService<A, R, S> extends Service<A, R> {

    void process(A request, @NotNull Consumer<? super R> callback, @NotNull Core.ApplicationProcessingContext<? super A, ? extends R, S> processingContext) throws YieldException;

    void onLoad(@NotNull Core<?, ?> core);

    void onUnload(@NotNull Core<?, ?> core);

    class YieldException extends RuntimeException {

        @Override
        public final StackTraceElement[] getStackTrace() {
            return new StackTraceElement[]{new StackTraceElement(YieldException.class.getCanonicalName(), "getStackTrace", "ProcessingService.java", 23)};
        }

    }

}
