package com.myzone.archivemanager.data;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

/**
 * @author myzone
 * @date 9/7/13 11:51 AM
 */
public class InMemoryDataAccessorTest {

    private DataAccessor<Point> accessor;

    @Before
    public void setUp() throws Exception {
        accessor = new InMemoryDataAccessor<>(Point.class);

        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        try {
            transaction.save(new PointImpl(0, 0));
            transaction.save(new PointImpl(1, 1));
            transaction.save(new PointImpl(2, 2));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();

            throw e;
        }
    }

    @Test
    public void testMutableDataCorruption1() throws Exception {
        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        Point mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
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
        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        Point mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        mutablePoint.setX(10);
        transaction.commit();

        transaction = accessor.beginTransaction();
        assertEquals(0, transaction.getAll().filter(point -> point.getX() == 10).count());
        transaction.rollback();
    }

    @Test
    public void testMutableDataCorruption3() throws Exception {
        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        Point mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
        assertNotNull(mutablePoint);
        mutablePoint.setX(10);
        transaction.rollback();

        transaction = accessor.beginTransaction();
        assertEquals(0, transaction.getAll().filter(point -> point.getX() == 10).count());
        transaction.rollback();
    }

    @Test
    public void testDataCorruption() throws Exception {
        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        assertEquals(1, transaction.getAll().filter(point -> point.getX() == 0).count());
        Point mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
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

        DataAccessor.Transaction<Point> transaction1 = executor.submit(() -> {
            DataAccessor.Transaction<Point> innerTransaction = accessor.beginTransaction();
            Point innerMutablePoint = innerTransaction.getAll().filter(point -> point.getX() == 0).findAny().get();
            assertNotNull(innerMutablePoint);
            innerMutablePoint.setX(10);
            innerTransaction.update(innerMutablePoint);
            return innerTransaction;
        }).get();

        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        Point mutablePoint = transaction.getAll().filter(point -> point.getX() == 0).findAny().get();
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

        DataAccessor.Transaction<Point> transaction = accessor.beginTransaction();
        Point mutablePoint = transaction.getAll().filter((point) -> point.getX() == 1).findFirst().get();
        transaction.commit();

        DataAccessor.Transaction<Point> transaction2 = executor.submit(() -> {
            DataAccessor.Transaction<Point> innerTransaction = accessor.beginTransaction();
            mutablePoint.setX(10);
            innerTransaction.update(mutablePoint);
            return innerTransaction;
        }).get();

        DataAccessor.Transaction<Point> transaction1 = accessor.beginTransaction();
        assertEquals(1, mutablePoint.getX());
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
        ExecutorService executor = Executors.newSingleThreadExecutor();

        DataAccessor<Point> accessor1 = new InMemoryDataAccessor<>(Point.class);
        DataAccessor<Point> accessor2 = new InMemoryDataAccessor<>(Point.class);

        PointImpl pointImpl = new PointImpl(10, 10);

        DataAccessor.Transaction<Point> transaction1 = accessor1.beginTransaction();
        transaction1.save(pointImpl);
        transaction1.commit();

        DataAccessor.Transaction<Point> transaction2 = accessor2.beginTransaction();
        transaction2.save(pointImpl);
        transaction2.commit();

        DataAccessor.Transaction<Point> transaction3 = accessor1.beginTransaction();
        pointImpl.setY(100);
        transaction3.update(pointImpl);

        executor.submit(() -> {
            DataAccessor.Transaction<Point> transaction4 = accessor2.beginTransaction();
            assertEquals(10, pointImpl.getY());
            transaction4.rollback();
        });

        transaction3.commit();

        DataAccessor.Transaction<Point> transaction5 = accessor2.beginTransaction();
        assertEquals(100, pointImpl.getY());
        transaction5.rollback();
    }

    @Test
    public void testContinue() throws Exception {
        PointImpl pointImpl1 = new PointImpl(10, 10);
        PointImpl pointImpl2 = new PointImpl(14, 15);

        accessor.beginTransaction()
                .save(pointImpl1)
                .and()
                .save(pointImpl2)
                .and()
                .commit();

        assertTrue(accessor.beginTransaction()
                        .getAll()
                        .collect(toSet())
                        .containsAll(asList(pointImpl1, pointImpl2)));
    }


    private static interface Point {

        int getX();

        void setX(int x);

        int getY();

        void setY(int y);

    }

    private static class PointImpl implements Point {

        private int x;
        private int y;

        private PointImpl(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Point)) return false;

            Point that = (Point) o;

            if (getX() != that.getX()) return false;
            if (getY() != that.getY()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = getX();
            result = 31 * result + getY();
            return result;
        }

        @Override
        public String toString() {
            return "PointImpl{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

    }

}
