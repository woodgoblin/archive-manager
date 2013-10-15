package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.ProcessingService;
import com.myzone.archivemanager.model.User;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 10/14/13 10:25 AM
 */
public class GlobalsService implements ProcessingService<Void, GlobalsService.Globals, Void> {

    private final Globals globals;

    public GlobalsService(Optional<User.CloseableSession> currentSession, Clock currentClock) {
        globals = new GlobalsProvider(currentSession, currentClock);
    }

    @Override
    public void process(Void request, @NotNull Consumer<? super Globals> callback, @NotNull Core.ApplicationProcessingContext<? super Void, ? extends Globals, Void> processingContext) throws YieldException {
        callback.accept(globals);
    }

    @Override
    public void onLoad(@NotNull Core<?, ?> core) {

    }

    @Override
    public void onUnload(@NotNull Core<?, ?> core) {

    }

    public interface Globals {

        Property<Optional<User.CloseableSession>> getCurrentSession();

        ObservableValue<Clock> getCurrentClock();

    }

    protected static class GlobalsProvider implements Globals {

        private final Property<Optional<User.CloseableSession>> currentSession;
        private final ReadOnlyProperty<Clock> currentClock;

        public GlobalsProvider(Optional<User.CloseableSession> currentSession, Clock currentClock) {
            this.currentSession = new SimpleObjectProperty<>(currentSession);
            this.currentClock = new ReadOnlyObjectWrapper<>(currentClock);
        }

        @Override
        public Property<Optional<User.CloseableSession>> getCurrentSession() {
            return currentSession;
        }

        @Override
        public ObservableValue<Clock> getCurrentClock() {
            return currentClock;
        }

    }

}
