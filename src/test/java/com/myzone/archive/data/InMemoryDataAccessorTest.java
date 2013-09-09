package com.myzone.archive.data;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * @author myzone
 * @date 9/7/13 11:51 AM
 */
public class InMemoryDataAccessorTest {

    private DataAccessor<MutablePoint> accessor;

    @Before
    public void setUp() throws Exception {
        accessor = new InMemoryDataAccessor<>(new File("ads"), MutablePoint.class);

        DataAccessor.Transaction<MutablePoint> transaction = accessor.beginTransaction();
        try {
            transaction.save(new MutablePoint(0, 0));
            transaction.save(new MutablePoint(1, 1));
            transaction.save(new MutablePoint(2, 2));

            transaction.commit();
        } catch (Exception e) {
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
