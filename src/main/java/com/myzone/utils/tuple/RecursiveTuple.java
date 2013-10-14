package com.myzone.utils.tuple;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/14/13 5:25 PM
 */
public class RecursiveTuple<D, T extends Tuple> implements Tuple<D, T> {

    private D data;
    private final T next;

    public RecursiveTuple(D data, T next) {
        this.data = data;
        this.next = next;
    }

    @Override
    public D get() {
        return data;
    }

    @Override
    public void set(D data) {
        this.data = data;
    }

    @NotNull
    @Override
    public T next() {
        return next;
    }

}
