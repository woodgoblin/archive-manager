package com.myzone.utils;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/8/13 4:46 PM
 */
public class RecursiveImmutableTuple<D, T extends ImmutableTuple> implements ImmutableTuple<D, T> {

    private final D data;
    private final T next;

    public RecursiveImmutableTuple(D data, T next) {
        this.data = data;
        this.next = next;
    }

    @Override
    public D get() {
        return data;
    }

    @NotNull
    @Override
    public T next() {
        return next;
    }

}
