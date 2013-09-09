package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/6/13 10:05 AM
 */
public interface Activity<N> {

    void onLoad(@NotNull Core.ApplicationGraphicsContext<? super N> graphicsContext);

    void onUnload(@NotNull Core.ApplicationGraphicsContext<? super N> graphicsContext);

}
