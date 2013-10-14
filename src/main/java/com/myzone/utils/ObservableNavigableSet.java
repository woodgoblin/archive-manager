package com.myzone.utils;

import javafx.collections.ObservableSet;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableSet;

/**
 * @author myzone
 * @date 10/12/13 2:48 AM
 */
public interface ObservableNavigableSet<E> extends NavigableSet<E>, ObservableSet<E> {

    @NotNull
    @Override
    ObservableNavigableSet<E> descendingSet();

    @NotNull
    @Override
    ObservableNavigableSet<E> headSet(E toElement);

    @NotNull
    @Override
    ObservableNavigableSet<E> headSet(E toElement, boolean inclusive);

    @NotNull
    @Override
    ObservableNavigableSet<E> tailSet(E fromElement);

    @NotNull
    @Override
    ObservableNavigableSet<E> tailSet(E fromElement, boolean inclusive);

    @NotNull
    @Override
    ObservableNavigableSet<E> subSet(E fromElement, E toElement);

    @NotNull
    @Override
    ObservableNavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

}
