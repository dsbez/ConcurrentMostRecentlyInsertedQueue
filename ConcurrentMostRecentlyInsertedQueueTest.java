import java.util.AbstractQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program implements the thread-safe version of queue, that uses locks while modifying it( inserting new
 * elements or removing them). The purpose of this queue is to store the N most recently inserted elements.
 * The queue is bounded in size. If the queue is already full (Queue#size() == capacity),  the oldest element,
 * that was inserted (the head), should be evicted, and then the new element can be added at the tail.
 * @version 2.01 2016-09-25
 * @author Denis Bezschastnuy
 */

public class ConcurrentMostRecentlyInsertedQueueTest {
    public static void main(String[] args) {
        Queue<Integer> queue = new ConcurrentMostRecentlyInsertedQueue<>(5);

        Runnable producer = () -> {
            try {
                while (true) {
                    queue.offer(1);
                    queue.offer(2);
                    queue.offer(3);
                    queue.offer(4);
                    queue.offer(5);
                    System.out.println(Thread.currentThread().getName()+" thread has finished work");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {

            }
        };

        Runnable  consumer= () -> {
            while (true)
            {
                System.out.println(Thread.currentThread().getName()+" thread has finished work");
                System.out.println("poll element: " + queue.poll());
            }
        };

        Thread producerThread = new Thread(producer, "producer");
        Thread consumerThread = new Thread(consumer, "consumer");
        producerThread.start();
        consumerThread.start();
    }
}

/**
 A first-in, first-out bounded collection.
 */

class ConcurrentMostRecentlyInsertedQueue<E> extends AbstractQueue<E> {

    private final Queue<E> elements;
    private int modcount;
    private final int capacity;
    private final Lock queueLock;
    private final Condition isEmpty;

    /**
     Constructs an empty queue.
     @param capacity the maximum capacity of the queue
     */

    public ConcurrentMostRecentlyInsertedQueue(final int capacity) {
        this.elements = new LinkedList<>();
        this.capacity = capacity;
        queueLock = new ReentrantLock();
        isEmpty = queueLock.newCondition();
    }

    @Override
    public boolean offer(E newElement) {
        queueLock.lock();
        try {
            assert newElement != null;

            if (elements.size() < capacity) {
                elements.add(newElement);
            } else {
                elements.remove();
                elements.add(newElement);
            }

            modcount++;
            isEmpty.signalAll();

            return true;
        }
        finally {
            queueLock.unlock();
        }
    }

    @Override
    public E poll() {
        queueLock.lock();
        try {
            while (elements.isEmpty()) {
                try {
                    isEmpty.await();
                } catch (InterruptedException e) {

                }
            }

            E result = elements.poll();
            modcount++;

            return result;

        }  finally {
            queueLock.lock();
        }
    }

    @Override
    public E peek() {
        if (elements.size() == 0) return null;

        return elements.peek();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new QueueIterator();
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public String toString() {
        return "queue.size(): " + elements.size() + ", contents (head -> tail): " +  elements;
    }


    private class QueueIterator implements Iterator<E> {

        private Iterator<E> iterator;
        private final int modcountAtConstruction;
        private int offset;

        public QueueIterator() {
            modcountAtConstruction = modcount;
            iterator = elements.iterator();
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();

            E result = iterator.next();
            offset++;

            return result;
        }

        @Override
        public boolean hasNext() {
            if (modcount != modcountAtConstruction)
                throw new ConcurrentModificationException();

            return offset < elements.size();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}



