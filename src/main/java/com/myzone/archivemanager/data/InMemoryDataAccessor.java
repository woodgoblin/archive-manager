package com.myzone.archivemanager.data;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 9/6/13 10:28 PM
 */
public class InMemoryDataAccessor<T> implements DataAccessor<T> {

    private static final Unsafe UNSAFE = getUnsafe();
    private static final ThreadLocal<Map<Class<?>, IdentityHashMap<?, ?>>> TRANSACTIONAL_DATA = new ThreadLocal<Map<Class<?>, IdentityHashMap<?, ?>>>() {

        @Override
        protected Map<Class<?>, IdentityHashMap<?, ?>> initialValue() {
            return new HashMap<>();
        }

    };

    @SuppressWarnings("unchecked")
    private static <T> IdentityHashMap<T, T> getIdentities(Class<T> clazz) {
        Map<Class<?>, IdentityHashMap<?, ?>> allIdentities = TRANSACTIONAL_DATA.get();

        IdentityHashMap<?, ?> classIdentities = allIdentities.get(clazz);
        if (classIdentities == null) {
            classIdentities = new IdentityHashMap<>();

            allIdentities.put(clazz, classIdentities);
        }

        return (IdentityHashMap<T, T>) classIdentities;
    }

    private final Class<T> clazz;

    private final ReadWriteLock lock;
    private volatile IdentityHashMap<T, Integer> cache;

    public InMemoryDataAccessor(Class<T> clazz) {
        this.clazz = clazz;

        lock = new ReentrantReadWriteLock(true);
        cache = new IdentityHashMap<>();

        modifyClass(clazz);
    }

    private void modifyClass(Class<T> clazz) {
        // todo: i think it's impossible
    }

    @Override
    public Transaction<T> beginTransaction() {
        return new Transaction<T>() {

            private final IdentityHashMap<T, Boolean> updated;
            private final IdentityHashMap<T, Integer> localCache;

            /* Transaction */ {
                updated = new IdentityHashMap<>();

                lock.readLock().lock();
                try {
                    localCache = new IdentityHashMap<>(cache);

                    IdentityHashMap<T, T> identities = getIdentities(clazz);
                    for (T o : cache.keySet()) {
                        T copy = newInstance(clazz);

                        merge(o, copy);

                        identities.put(o, copy);
                    }
                } finally {
                    lock.readLock().unlock();
                }
            }

            @Override
            public Stream<T> getAll() {
                return localCache.keySet().stream();
            }

            @Override
            public void save(T o) {
                localCache.put(o, 0);
                updated.put(o, false);
            }

            @Override
            public void update(T o) {
                localCache.put(o, localCache.get(o) + 1);
                updated.put(o, true);
            }

            @Override
            public void delete(T o) {
                localCache.remove(o);
                updated.put(o, false);
            }

            @Override
            public void commit() throws DataModificationException {
                lock.writeLock().lock();

                try {
                    for(T updatedObject : updated.keySet()) {
                        Integer sharedRevision = cache.get(updatedObject);
                        Integer localRevision  = localCache.get(updatedObject);

                        if (localRevision == null) {
                            if (sharedRevision == null) {
                                throw new DataModificationException(); // updatedObject has been removed before
                            }
                        } else {
                            if (sharedRevision != null && localRevision <= sharedRevision) {
                                throw new DataModificationException(); // updatedObject has been updated before
                            }
                        }
                    }

                    IdentityHashMap<T, T> identities = getIdentities(clazz);
                    updated.forEach((updatedObject, wasUpdated) -> {
                        if (wasUpdated) {
                            merge(identities.get(updatedObject), updatedObject);
                        }
                    });
                    identities.clear();

                    cache = localCache;
                } finally {
                    lock.writeLock().unlock();
                }
            }

            @Override
            public void rollback() {
                getIdentities(clazz).clear();
            }

        };
    }

    @SuppressWarnings("unchecked")
    protected static <T> T newInstance(Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T> void merge(T from, T to) {
        if (!from.getClass().equals(to.getClass())) {
            throw new IllegalArgumentException();
        }

        Field[] fields = to.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                field.set(to, field.get(from));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return  (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class DataObject {

        @SuppressWarnings("unchecked")
        protected <T extends DataObject> T transactional(T o) {
            return getIdentities((Class<T>) o.getClass()).getOrDefault(o, o);
        }

    }

}
