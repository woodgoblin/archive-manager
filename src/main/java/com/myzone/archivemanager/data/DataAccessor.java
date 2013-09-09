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

        void save(T o);

        void update(T o);

        void delete(T o);

        void commit() throws DataModificationException;

        void rollback();

    }

    public class DataModificationException extends Exception {

    }

}
