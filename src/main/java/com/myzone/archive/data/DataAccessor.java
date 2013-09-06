package com.myzone.archive.data;

import java.io.Closeable;
import java.util.stream.Stream;

/**
 * @author myzone
 * @date 9/6/13 10:09 AM
 */
public interface DataAccessor<T> {

    Transaction<T> beginTransaction();

    public interface Transaction<T> {

        Stream<T> getAll() throws DataAccessException;

        void save(T o) throws DataAccessException;

        void update(T o) throws DataAccessException;

        void delete(T o) throws DataAccessException;

        void commit() throws DataModificationException;

        void rollback();

    }

    public class DataAccessException extends Exception {

    }

    public class DataModificationException extends Exception {

    }

}
