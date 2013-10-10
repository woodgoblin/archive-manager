package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.ProcessingService;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author myzone
 * @date 9/14/13 5:09 PM
 */
public class ConcatService implements ProcessingService<String[], String, ConcatService.ConcatState> {

    @Override
    public void process(
            String[] request,
            @NotNull Consumer<? super String> callback,
            @NotNull Core.ApplicationProcessingContext<? super String[], ? extends String, ConcatState> processingContext
    ) throws YieldException {
        if (processingContext.getState() == null) {
            processingContext.setState(new ConcatState());
        }

        ConcatState state = processingContext.getState();

        state.getResultBuilder().append(request[state.getStep()]);
        state.setStep(state.getStep() + 1);

        if (state.getStep() == request.length) {
            callback.accept(state.getResultBuilder().toString());
        } else {
            processingContext.yield(request, callback);
        }
    }

    @Override
    public void onLoad(@NotNull Core<?, ?> core) {

    }

    @Override
    public void onUnload(@NotNull Core<?, ?> core) {

    }

    protected static class ConcatState {

        private int step;
        private StringBuilder resultBuilder;

        private ConcatState() {
            step = 0;
            resultBuilder = new StringBuilder("");
        }

        private int getStep() {
            return step;
        }

        private void setStep(int step) {
            this.step = step;
        }

        private StringBuilder getResultBuilder() {
            return resultBuilder;
        }

        private void setResultBuilder(StringBuilder resultBuilder) {
            this.resultBuilder = resultBuilder;
        }
    }

}
