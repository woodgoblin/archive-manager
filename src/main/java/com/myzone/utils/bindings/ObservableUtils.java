package com.myzone.utils.bindings;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author myzone
 * @date 10/15/13 12:42 AM
 */
public class ObservableUtils {

    public static <T, K> ObservableValue<K> mapObservable(ObservableValue<T> originObservable, Function<T, K> mapper) {
        return new ObservableValue<K>() {

            private final Map<ChangeListener<? super K>, ChangeListener<T>> changeListenersMap = new HashMap<>();
            private final Map<InvalidationListener, InvalidationListener> invalidationListenersMap = new HashMap<>();

            @Override
            public void addListener(ChangeListener<? super K> changeListener) {
                if (!changeListenersMap.containsKey(changeListener)) {
                    ChangeListener<T> mappedChangeListener = (observableValue, oldValue, newValue) -> {
                        changeListener.changed(this, mapper.apply(oldValue), mapper.apply(newValue));
                    };

                    changeListenersMap.put(changeListener, mappedChangeListener);

                    originObservable.addListener(mappedChangeListener);
                }
            }

            @Override
            public void removeListener(ChangeListener<? super K> changeListener) {
                ChangeListener<T> mappedChangeListener = changeListenersMap.get(changeListener);

                if (mappedChangeListener != null) {
                    originObservable.removeListener(mappedChangeListener);
                }
            }

            @Override
            public void addListener(InvalidationListener originInvalidationListener) {
                if (!invalidationListenersMap.containsKey(originInvalidationListener)) {
                    InvalidationListener mappedInvalidationListener = observable -> {
                            originInvalidationListener.invalidated(this);
                    };

                    invalidationListenersMap.put(originInvalidationListener, mappedInvalidationListener);

                    originObservable.addListener(mappedInvalidationListener);
                }
            }

            @Override
            public void removeListener(InvalidationListener originInvalidationListener) {
                InvalidationListener mappedInvalidationListener = invalidationListenersMap.get(originInvalidationListener);

                originObservable.addListener(mappedInvalidationListener);
            }

            @Override
            public K getValue() {
                return mapper.apply(originObservable.getValue());
            }

        };
    }

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
