package com.myzone.archivemanager.activities;

import com.myzone.archivemanager.core.Activity;
import com.myzone.archivemanager.core.Core;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/22/13 12:50 PM
 */
public abstract class StatusActivity<N> implements Activity<N> {

    protected final SimpleObjectProperty<Status> status;

    protected StatusActivity() {
        status = new SimpleObjectProperty<>(null);
    }

    @Override
    public void onLoad(@NotNull Core.ApplicationGraphicsContext<? super N> graphicsContext) {
        status.set(Status.STARTED);
    }

    @Override
    public void onUnload(@NotNull Core.ApplicationGraphicsContext<? super N> graphicsContext) {
        status.set(Status.STOPPED);
    }

    public ObservableValue<Status> getStatus() {
        return status;
    }

    public enum Status {
        STARTED,
        DONE,
        STOPPED
    }

}
