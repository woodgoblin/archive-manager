package com.myzone.archive.core;

import org.jetbrains.annotations.NotNull;

/**
 * User: myzone
 * Date: 9/8/13
 * Time: 3:54 PM
 */
public interface Service<A, R> {

    void onLoad(@NotNull Core<?> core);

    void onUnload(@NotNull Core<?> core);

}
