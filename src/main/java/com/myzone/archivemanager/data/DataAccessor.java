package com.myzone.archivemanager.data;

import java.util.stream.Stream;

/**
 * @author myzone
 * @date 9/6/13 10:09 AM
 */
public interface DataAccessor<T> {

    Transaction<T> beginTransaction();

    public interface Transaction<T> {

        Stream<T> getAll();

        Continue<T> save(T o);

        Continue<T> update(T o);

        Continue<T> delete(T o);

        void commit() throws DataModificationException;

        void rollback();

    }

    interface Continue<T> {

        Transaction<T> and();

    }

    public class DataModificationException extends Exception {

    }

}
