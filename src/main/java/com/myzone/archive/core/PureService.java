package com.myzone.archive.core;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/8/13 3:14 PM
 */
public interface PureService<A, R> extends Service<A, R> {

    void process(A request, @NotNull Function<? super R, Void> callback);

}
