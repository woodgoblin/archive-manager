package com.myzone.utils;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author myzone
 * @date 10/15/13 12:42 AM
 */
public class ObservableCollections {

    public static <T> ObservableList<T> toReadonlyObservableList(ObservableSet<? extends T> navigableSet) {
        ObservableList<T> result = new ObservableListWrapper<>(new ArrayList<>());
        InvalidationListener invalidationListener = observable -> {
            result.clear();
            result.addAll(navigableSet.stream().collect(Collectors.toList()));
        };

        invalidationListener.invalidated(navigableSet);
        navigableSet.addListener(invalidationListener);

        return new ReadOnlyListWrapper<>(result);
    }

}
