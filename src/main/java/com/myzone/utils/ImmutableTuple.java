package com.myzone.utils;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/8/13 4:41 PM
 */
public interface ImmutableTuple<D, T extends ImmutableTuple> {

    D get();

    @NotNull
    T next();

    public static enum End implements ImmutableTuple {

        END;

        @Override
        public Object get() {
            return null;
        }

        @NotNull
        @Override
        public ImmutableTuple next() {
            return this;
        }

    }

}
