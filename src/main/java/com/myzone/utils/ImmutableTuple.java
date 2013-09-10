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

    enum End implements ImmutableTuple<Object, End> {

        END;

        @Override
        public Object get() {
            return null;
        }

        @NotNull
        @Override
        public End next() {
            return null;
        }

    }

}
