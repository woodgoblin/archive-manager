package com.myzone.archive.core;

/**
 * @author: myzone
 * @date: 9/6/13 10:05 AM
 */
public interface Activity<N> {

    void onLoad(Core.ApplicationGraphicsContext<? super N> graphicsContext);

    void onUnload(Core.ApplicationGraphicsContext<? super N> graphicsContext);

}
