# ConcurrentMostRecentlyInsertedQueue
Implementation of ConcurrentMostRecentlyInsertedQueue - a thread-safe version of MostRecentlyInsertedQueue

 * This program implements the thread-safe version of queue, that uses locks while modifying it( inserting new
 * elements or removing them). The purpose of this queue is to store the N most recently inserted elements.
 * The queue is bounded in size. If the queue is already full (Queue#size() == capacity),  the oldest element,
 * that was inserted (the head), should be evicted, and then the new element can be added at the tail.
