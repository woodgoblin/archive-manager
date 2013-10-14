package com.myzone.utils.tuple;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/14/13 5:23 PM
 */
public interface Tuple<D, T extends Tuple> extends ImmutableTuple<D, T> {

    void set(D data);

    enum End implements Tuple<Object, End> {

        END;

        @Override
        public Object get() {
            return null;
        }

        @Override
        public void set(Object data) {
        }

        @NotNull
        @Override
        public End next() {
            return this;
        }

    }

}
