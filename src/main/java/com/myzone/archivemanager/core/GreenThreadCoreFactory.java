package com.myzone.archivemanager.core;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/14/13 8:10 PM
 */
public class GreenThreadCoreFactory<N, D extends Core.DataProvider> implements ScheduledCore.Factory<N, D> {

    @NotNull
    @Override
    public ScheduledCore<N, D> create(
            @NotNull ScheduledCore.GraphicsContextProvider<N> graphicsContextProvider,
            @NotNull ScheduledCore.DataContextProvider<D> dataContextProvider
    ) {
        return new GreenTreadCore<>(graphicsContextProvider, dataContextProvider);
    }

}
