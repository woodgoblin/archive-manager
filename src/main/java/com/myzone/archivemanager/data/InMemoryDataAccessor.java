package com.myzone.archivemanager.data;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 9/6/13 10:28 PM
 */
public class InMemoryDataAccessor<T> implements DataAccessor<T> {

    private static final Unsafe UNSAFE = getUnsafe();

    private final Class<T> originalClass;
//    private final Class<T> proxyClass;

    private final ReadWriteLock lock;
    private volatile IdentityHashMap<T, Integer> cache;
    private final ThreadLocal<Transaction<T>> localTransaction;
    private final Map<Transaction<T>, IdentityHashMap<T, T>> identitiesMap;

    public InMemoryDataAccessor(Class<T> originalClass) {
        if (!originalClass.isInterface())
            throw new IllegalArgumentException("originalClass should be interface");

        this.originalClass = originalClass;

        lock = new ReentrantReadWriteLock(true);
        cache = new IdentityHashMap<>();
        localTransaction = new ThreadLocal<>();
        identitiesMap = new ConcurrentHashMap<>();
    }

    @Override
    public Transaction<T> beginTransaction() {
        return new InMemoryTransaction();
    }

    protected Transaction<T> currentTransaction() {
        return localTransaction.get();
    }

    private T wrap(T original) {
        return (T) Proxy.newProxyInstance(
                originalClass.getClassLoader(),
                new Class[]{originalClass},
                new SelectInvocationHandler(original)
        );
    }

    private T unwrap(T proxy) {
//        for (Method m : proxy.getClass().getMethods()) {
//            System.out.println(m.getName());
//        }

        return ((SelectInvocationHandler) Proxy.getInvocationHandler(proxy)).getOriginal();
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
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected class SelectInvocationHandler implements InvocationHandler {

        private final T original;

        public SelectInvocationHandler(T original) {
            this.original = original;
        }

        @Override
        public Object invoke(Object self, Method method, Object[] objects) throws Throwable {
            T realSelf = identitiesMap.getOrDefault(currentTransaction(), new IdentityHashMap<>()).getOrDefault(self, original);

            Object invoke1 = method.invoke(realSelf, objects);

            System.out.println("proxy " + realSelf + "." + method.getName() + "(" + Arrays.deepToString(objects) + ")" + ": " + invoke1);

            return invoke1;
        }

        public T getOriginal() {
            return original;
        }

    }

    protected class InMemoryTransaction implements Transaction<T> {

        private final Continue<T> defaultContinue;
        private final IdentityHashMap<T, Boolean> updated;
        private final IdentityHashMap<T, Integer> localCache;

        public InMemoryTransaction() {
            localTransaction.set(this);
            defaultContinue = () -> localTransaction.get();
            updated = new IdentityHashMap<>();

            lock.readLock().lock();
            try {
                localCache = new IdentityHashMap<>(cache);

                IdentityHashMap<T, T> identities = identitiesMap.get(this);

                if (identities == null) {
                    identities = new IdentityHashMap<>();

                    identitiesMap.put(this, identities);
                }

                for (T o : cache.keySet()) {
                    T original = unwrap(o);

                    T copy = (T) newInstance(original.getClass());
                    merge(original, copy);

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
        public Continue<T> save(T o) {
            o = wrap(o);

            localCache.put(o, 0);
            updated.put(o, false);

            return defaultContinue;
        }

        @Override
        public Continue<T> update(T o) {
            localCache.put(o, localCache.get(o) + 1);
            updated.put(o, true);

            return defaultContinue;
        }

        @Override
        public Continue<T> delete(T o) {
            localCache.remove(o);
            updated.put(o, false);

            return defaultContinue;
        }

        @Override
        public void commit() throws DataModificationException {
            lock.writeLock().lock();

            try {
                for (T updatedObject : updated.keySet()) {
                    Integer sharedRevision = cache.get(updatedObject);
                    Integer localRevision = localCache.get(updatedObject);

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

                Map<T, T> identities = identitiesMap.get(this);
                updated.forEach((updatedObject, wasUpdated) -> {
                    if (wasUpdated) {
                        T original = unwrap(updatedObject);
                        T copy = identities.get(updatedObject);

                        merge(copy, original);
                    }
                });
                identitiesMap.remove(this);

                cache = localCache;
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public void rollback() {
            identitiesMap.remove(this);
        }

    }

}
