package com.myzone.utils;

import com.sun.javafx.binding.SetExpressionHelper;
import com.sun.javafx.collections.SetListenerHelper;
import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * @author myzone
 * @date 10/12/13 2:50 AM
 */
public class ObservableNavigableSetWrapper<E> implements ObservableNavigableSet<E> {

    private final NavigableSet<E> origin;

    private SetListenerHelper<E> listenerHelper;

    public ObservableNavigableSetWrapper(@NotNull NavigableSet<E> origin) {
        this.origin = origin;
        this.listenerHelper = null;
    }

    protected ObservableNavigableSetWrapper(@NotNull NavigableSet<E> origin, @Nullable SetListenerHelper listenerHelper) {
        this.origin = origin;
        this.listenerHelper = listenerHelper;
    }

    @Override
    public E pollFirst() {
        E polled = origin.pollFirst();

        if (polled != null) {
            fireValueChangedEvent(() -> removedChange(polled));
        }

        return polled;
    }

    @Override
    public E pollLast() {
        E polled = origin.pollLast();

        if (polled != null) {
            fireValueChangedEvent(() -> removedChange(polled));
        }

        return polled;
    }


    @Override
    public Iterator<E> iterator() {
        return new ObservableIteratorWrapper(origin.iterator());
    }

    @Override
    public boolean add(E e) {
        boolean added = origin.add(e);

        if (added) {
            fireValueChangedEvent(() -> addedChange(e));
        }

        return added;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = origin.remove(o);

        if (removed) {
            fireValueChangedEvent(() -> removedChange((E) o));
        }

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean added = origin.addAll(c);

        if (added) {
            fireValueChangedEvents(() ->
                    origin
                            .stream()
                            .map(this::addedChange)
                            .collect(toList())
            );

        }

        return added;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<E> toBeRetained = this
                .stream()
                .filter((t) -> !c.contains(t))
                .collect(toList());

        boolean retained = origin.retainAll(c);

        if (retained) {
            fireValueChangedEvents(() ->
                    toBeRetained
                            .stream()
                            .map(this::removedChange)
                            .collect(toList())
            );

        }

        return retained;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = origin.removeAll(c);

        if (removed) {
            fireValueChangedEvents(() ->
                    c
                            .stream()
                            .map((e) -> removedChange((E) e))
                            .collect(toList())
            );
        }

        return removed;
    }

    @Override
    public void clear() {
        Set<E> copy = new HashSet<>(this);

        origin.clear();

        fireValueChangedEvents(() ->
                copy
                        .stream()
                        .map(this::removedChange)
                        .collect(toList())
        );
    }

    @NotNull
    @Override
    public ObservableNavigableSetWrapper<E> descendingSet() {
        return new ObservableNavigableSetWrapper<>(origin.descendingSet(), listenerHelper);
    }

    @NotNull
    @Override
    public Iterator<E> descendingIterator() {
        return new ObservableIteratorWrapper(origin.descendingIterator());
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> headSet(E toElement) {
        return new ObservableNavigableSetWrapper<>(origin.headSet(toElement, false), listenerHelper);
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ObservableNavigableSetWrapper<>(origin.headSet(toElement, inclusive), listenerHelper);
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> tailSet(E fromElement) {
        return new ObservableNavigableSetWrapper<>(origin.tailSet(fromElement, true), listenerHelper);
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ObservableNavigableSetWrapper<>(origin.tailSet(fromElement, inclusive), listenerHelper);
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> subSet(E fromElement, E toElement) {
        return new ObservableNavigableSetWrapper<>(origin.subSet(fromElement, true, toElement, false), listenerHelper);
    }

    @NotNull
    @Override
    public ObservableNavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ObservableNavigableSetWrapper<>(origin.subSet(fromElement, fromInclusive, toElement, toInclusive), listenerHelper);
    }

    @Override
    public E lower(E e) {
        return origin.lower(e);
    }

    @Override
    public E floor(E e) {
        return origin.floor(e);
    }

    @Override
    public E ceiling(E e) {
        return origin.ceiling(e);
    }

    @Override
    public E higher(E e) {
        return origin.higher(e);
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

    @Override
    public Object[] toArray() {
        return origin.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return origin.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return origin.containsAll(c);
    }

    @Override
    public E first() {
        return origin.first();
    }

    @Override
    public E last() {
        return origin.last();
    }

    @Nullable
    @Override
    public Comparator<? super E> comparator() {
        return origin.comparator();
    }

    @Override
    public void addListener(SetChangeListener<? super E> setChangeListener) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, setChangeListener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> setChangeListener) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, setChangeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, invalidationListener);
    }

    protected void fireValueChangedEvent(Supplier<SetChangeListener.Change<? extends E>> changeSupplier) {
        if (SetListenerHelper.hasListeners(listenerHelper)) {
            SetListenerHelper.fireValueChangedEvent(listenerHelper, changeSupplier.get());
        }
    }

    protected void fireValueChangedEvents(Supplier<List<SetChangeListener.Change<? extends E>>> changeSuppliers) {
        if (SetListenerHelper.hasListeners(listenerHelper)) {
            changeSuppliers.get().forEach((changeSupplier) -> SetListenerHelper.fireValueChangedEvent(listenerHelper, changeSupplier));
        }
    }

    protected SetExpressionHelper.SimpleChange<E> addedChange(E added) {
        SetExpressionHelper.SimpleChange<E> result = new SetExpressionHelper.SimpleChange<>(this);

        result.setAdded(added);

        return result;
    }

    protected SetExpressionHelper.SimpleChange<E> removedChange(E removed) {
        SetExpressionHelper.SimpleChange<E> result = new SetExpressionHelper.SimpleChange<>(this);

        result.setRemoved(removed);

        return result;
    }

    protected class ObservableIteratorWrapper implements Iterator<E> {

        private final Iterator<E> origin;

        private boolean filled = false;
        private E current;

        public ObservableIteratorWrapper(@NotNull Iterator<E> origin) {
            this.origin = origin;
        }

        @Override
        public boolean hasNext() {
            return origin.hasNext();
        }

        @Override
        public E next() {
            filled = true;
            current = origin.next();

            return current;
        }

        @Override
        public void remove() {
            origin.remove();

            fireValueChangedEvent(() -> removedChange(current));
        }

    }

}
