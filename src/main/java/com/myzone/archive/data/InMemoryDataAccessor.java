package com.myzone.archive.data;

import org.powermock.core.IdentityHashSet;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * User: myzone
 * Date: 9/6/13
 * Time: 10:28 PM
 */
public class InMemoryDataAccessor<T> implements DataAccessor<T> {

    private static final Unsafe UNSAFE = getUnsafe();
    private static final Set<Class<?>> MODIFIED_CLASSES = new CopyOnWriteArraySet<>();
    private static final ThreadLocal<Map<Class<?>, IdentityHashMap<?, ?>>> TRANSACTIONAL_DATA = new ThreadLocal<Map<Class<?>, IdentityHashMap<?, ?>>>() {

        @Override
        protected Map<Class<?>, IdentityHashMap<?, ?>> initialValue() {
            return new HashMap<>();
        }

    };

    private static <T> IdentityHashMap<T, T> getIdentities(Class<T> clazz) {
        Map<Class<?>, IdentityHashMap<?, ?>> allIdentities = TRANSACTIONAL_DATA.get();

        IdentityHashMap<?, ?> classIdentities = allIdentities.get(clazz);
        if (classIdentities == null) {
            classIdentities = new IdentityHashMap<>();
            classIdentities = allIdentities.putIfAbsent(clazz, classIdentities);
        }

        return (IdentityHashMap<T, T>) classIdentities;
    }

    private final File dataFile;
    private final Class<T> clazz;

    private final Lock lock;
    private volatile Set<T> cache;

    public InMemoryDataAccessor(File dataFile, Class<T> clazz) {
        this.dataFile = dataFile;
        this.clazz = clazz;

        lock = new ReentrantLock();
        cache = new IdentityHashSet<>();

        modifyClass(clazz);
    }

    private void modifyClass(Class<T> clazz) {
    }

    @Override
    public Transaction<T> beginTransaction() {
        lock.lock();

        return new Transaction<T>() {

            Set<T> cacheCopy = new HashSet<>(cache);
            IdentityHashSet<T> updated = new IdentityHashSet<>();

            {
                IdentityHashMap<T, T> identities = getIdentities(clazz);

                for (T o : cache) {
                    T copy = newInstance(clazz);

                    merge(o, copy);

                    identities.put(o, copy);
                }
            }

            @Override
            public Stream<T> getAll() throws DataAccessException {
                return cacheCopy.stream();
            }

            @Override
            public void save(T o) throws DataAccessException {
                cacheCopy.add(o);
            }

            @Override
            public void update(T o) throws DataAccessException {
                updated.add(o);
            }

            @Override
            public void delete(T o) throws DataAccessException {
                cacheCopy.remove(o);
            }

            @Override
            public void commit() throws DataModificationException {
                cache = new CopyOnWriteArraySet<>(cacheCopy);

                IdentityHashMap<T, T> identities = getIdentities(clazz);
                for (T updatedObject : updated) {
                    merge(identities.get(updatedObject), updatedObject);
                }
                identities.clear();
                lock.unlock();
            }

            @Override
            public void rollback() {
                getIdentities(clazz).clear();
                lock.unlock();
            }

        };
    }

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

        protected <T extends DataObject> T transactional(T o) {
            return (T) InMemoryDataAccessor.getIdentities((Class<T>) o.getClass()).getOrDefault(o, o);
        }

    }

}
