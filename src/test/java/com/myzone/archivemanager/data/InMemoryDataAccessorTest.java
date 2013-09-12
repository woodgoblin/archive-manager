package com.myzone.archivemanager.data;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

/**
 * @author myzone
 * @date 9/7/13 11:51 AM
 */
public class InMemoryDataAccessorTest {

    private DataAccessor<MutablePoint> accessor;

    @Before
    public void setUp() throws Exception {
        accessor = new InMemoryDataAccessor<>(MutablePoint.class);

        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        try {
            transaction.save(new MutablePoint(0, 0));
            transaction.save(new MutablePoint(1, 1));
            transaction.save(new MutablePoint(2, 2));

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();

            transaction.rollback();

            throw e;
        }
    }

    @Test
    public void testMutableDataCorruption1() throws Exception {
        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        MutablePoint mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        mutablePoint.setX(10);
        transaction.update(mutablePoint);
        transaction.commit();

        transaction = accessor.beginTransaction();
        assertEquals(1, transaction.getAll().filter(point -> point.getX() == 10).count());
        transaction.rollback();
    }

    @Test
    public void testMutableDataCorruption2() throws Exception {
        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        MutablePoint mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        mutablePoint.setX(10);
        transaction.commit();

        transaction = accessor.beginTransaction();
        assertEquals(0, transaction.getAll().filter(point -> point.getX() == 10).count());
        transaction.rollback();
    }

    @Test
    public void testMutableDataCorruption3() throws Exception {
        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        MutablePoint mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        mutablePoint.setX(10);
        transaction.rollback();

        transaction = accessor.beginTransaction();
        assertEquals(0, transaction.getAll().filter(point -> point.getX() == 10).count());
        transaction.rollback();
    }

    @Test
    public void testDataCorruption() throws Exception {
        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        assertEquals(1, transaction.getAll().filter(point -> point.getX() == 0).count());
        MutablePoint mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        transaction.delete(mutablePoint);
        assertEquals(0, transaction.getAll().filter(point -> point.getX() == 0).count());
        transaction.rollback();

        transaction = accessor.beginTransaction();
        assertEquals(1, transaction.getAll().filter(point -> point.getX() == 0).count());
        transaction.rollback();
    }

    @Test(expected = DataAccessor.DataModificationException.class)
    public void testRace() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        DataAccessor.Transaction<MutablePoint> transaction1 = executor.submit(() -> {
            DataAccessor.Transaction<MutablePoint> innerTransaction = accessor.beginTransaction();
            MutablePoint innerMutablePoint = innerTransaction.getAll().filter(point -> point.getX() == 0).findAny().get();
            assertNotNull(innerMutablePoint);
            innerMutablePoint.setX(10);
            innerTransaction.update(innerMutablePoint);
            return innerTransaction;
        }).get();

        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        MutablePoint mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        assertEquals(0, mutablePoint.getX());
        mutablePoint.setX(100);

        executor.submit(() -> {
            transaction1.commit();
            return null;
        }).get();

        transaction.update(mutablePoint);
        transaction.commit();
    }

    @Test(expected = DataAccessor.DataModificationException.class)
    public void testRaceWithSave() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        MutablePoint mutablePoint = new MutablePoint(10, 10);
        transaction.save(mutablePoint);
        transaction.commit();

        DataAccessor.Transaction<MutablePoint> transaction2 = executor.submit(() -> {
            DataAccessor.Transaction<MutablePoint> innerTransaction = accessor.beginTransaction();
            mutablePoint.setX(10);
            innerTransaction.update(mutablePoint);
            return innerTransaction;
        }).get();

        DataAccessor.Transaction<MutablePoint> transaction1 = accessor.beginTransaction();
        assertEquals(10, mutablePoint.getX());
        mutablePoint.setX(100);

        executor.submit(() -> {
            transaction2.commit();
            return null;
        }).get();

        transaction1.update(mutablePoint);
        transaction1.commit();
    }

    @Test
    public void testManyAccessors() throws Exception {
        DataAccessor<MutablePoint> accessor1 = new InMemoryDataAccessor<>(MutablePoint.class);
        DataAccessor<MutablePoint> accessor2 = new InMemoryDataAccessor<>(MutablePoint.class);

        MutablePoint mutablePoint = new MutablePoint(10, 10);

        DataAccessor.Transaction<MutablePoint> transaction1 = accessor1.beginTransaction();
        transaction1.save(mutablePoint);
        transaction1.commit();

        DataAccessor.Transaction<MutablePoint> transaction2 = accessor2.beginTransaction();
        transaction2.save(mutablePoint);
        transaction2.commit();

        DataAccessor.Transaction<MutablePoint> transaction3 = accessor1.beginTransaction();
        mutablePoint.setY(100);
        transaction3.update(mutablePoint);

        DataAccessor.Transaction<MutablePoint> transaction4 = accessor2.beginTransaction();
        assertEquals(10, mutablePoint.getY());
        transaction4.rollback();

        transaction3.commit();

        DataAccessor.Transaction<MutablePoint> transaction5 = accessor2.beginTransaction();
        assertEquals(100, mutablePoint.getY());
        transaction5.rollback();
    }

    private static class MutablePoint extends InMemoryDataAccessor.DataObject implements Serializable {

        private int x;
        private int y;

        private MutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int getX() {
            return transactional(this).x;
        }

        private void setX(int x) {
            transactional(this).x = x;
        }

        private int getY() {
            return transactional(this).y;
        }

        private void setY(int y) {
            transactional(this).y = y;
        }

        @Override
        public String toString() {
            return "MutablePoint{" +
                    "x=" + transactional(this).x +
                    ", y=" + transactional(this).y +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MutablePoint that = (MutablePoint) o;

            if (transactional(this).x != that.getX()) return false;
            if (transactional(this).y != that.getY()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = transactional(this).x;
            result = 31 * result + transactional(this).y;
            return result;
        }
    }

}
