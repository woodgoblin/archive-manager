package com.myzone.utils;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author myzone
 * @date 10/15/13 1:09 AM
 */
public class ReadOnlyObservableNavigableSetWrapper<E> implements ObservableNavigableSet<E> {

    protected final ObservableNavigableSet<E> origin;

    public ReadOnlyObservableNavigableSetWrapper(@NotNull ObservableNavigableSet<E> origin) {
        this.origin = origin;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIteratorWrapper<>(origin.iterator());
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> descendingSet() {
        return new ObservableNavigableSetWrapper<>(origin.descendingSet());
    }

    @NotNull
    @Override
    public Iterator<E> descendingIterator() {
        return new ReadOnlyIteratorWrapper<>(origin.descendingIterator());
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> headSet(E toElement) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.headSet(toElement, false));
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.headSet(toElement, inclusive));
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> tailSet(E fromElement) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.tailSet(fromElement, true));
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.tailSet(fromElement, inclusive));
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> subSet(E fromElement, E toElement) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.subSet(fromElement, true, toElement, false));
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ReadOnlyObservableNavigableSetWrapper<>(origin.subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    public E lower(E e) {
        return origin.lower(e);
    }

    public E floor(E e) {
        return origin.floor(e);
    }

    public E ceiling(E e) {
        return origin.ceiling(e);
    }

    public E higher(E e) {
        return origin.higher(e);
    }

    @Override
    public E pollFirst() {
        return origin.pollFirst();
    }

    @Override
    public E pollLast() {
        return origin.pollLast();
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return origin.comparator();
    }

    @Override
    public E first() {
        return origin.first();
    }

    @Override
    public E last() {
        return origin.last();
    }

    @Override
    public Spliterator<E> spliterator() {
        return origin.spliterator();
    }

    @Override
    public int size() {
        return origin.size();
    }

    @Override
    public boolean isEmpty() {
        return origin.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return origin.contains(o);
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return origin.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(T[] a) {
        return origin.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return origin.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return origin.equals(o);
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    public void addListener(SetChangeListener<? super E> setChangeListener) {
        origin.addListener(setChangeListener);
    }

    public void removeListener(SetChangeListener<? super E> setChangeListener) {
        origin.removeListener(setChangeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        origin.addListener(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        origin.removeListener(invalidationListener);
    }

    protected static class ReadOnlyIteratorWrapper<E> implements Iterator<E> {

        private final Iterator<E> iterator;

        public ReadOnlyIteratorWrapper(@NotNull Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }

    }

}
