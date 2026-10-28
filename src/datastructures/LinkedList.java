package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;


// singly linked list used to store edges in the adjacency list
// each node holds data and a pointer to the next node
public class LinkedList<T> implements Iterable<T> {

    // inner node class - holds the data and points to the next node
    private static class ListNode<T> {
        T data;
        ListNode<T> next;
        // Note - could maybe change this to pass next in the constructor too
        ListNode(T data) { this.data = data; this.next = null; }
    }

    private ListNode<T> head;
    private ListNode<T> tail;
    private int size;

    // Note - could maybe add parameters here later
    /** creates an empty linked list */
    public LinkedList() { head = null; tail = null; size = 0; }


    /**
     * adds an item to the end of the list
     *
     * @param item the item to add
     */
    public void addLast(T item) {
        ListNode<T> newNode = new ListNode<>(item);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * alias for addLast, just a shorter name to call
     *
     * @param item the item to add
     */
    public void add(T item) { addLast(item); }


    /**
     * removes the first time this item appears in the list
     *
     * @param item the item to look for and remove
     * @return true if it was found and removed, false if not found
     */
    public boolean remove(T item) {
        if (head == null) {
            return false; // list is empty, nothing to remove
        }

        if (head.data.equals(item)) {
            // item is at the head so just move head forward
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        ListNode<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                if (current.next == tail) {
                    tail = current; // removing the tail so update it
                }
                current.next = current.next.next; // skip over the found node
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }


    /**
     * removes all elements from the list that match the given condition
     *
     * @param predicate a condition - any element that matches this gets removed
     * @return true if at least one element was removed
     */
    public boolean removeIf(Predicate<T> predicate) {
        boolean removed = false;

        // remove from the front as long as head matches
        while (head != null && predicate.test(head.data)) {
            head = head.next;
            size--;
            removed = true;
        }

        if (head == null) { tail = null; return removed; }

        ListNode<T> current = head;
        while (current.next != null) {
            if (predicate.test(current.next.data)) {
                if (current.next == tail) {
                    tail = current;
                }
                current.next = current.next.next;
                size--;
                removed = true;
            } else {
                current = current.next;
            }
        }

        // fix tail pointer just in case
        if (head != null) {
            ListNode<T> c = head;
            while (c.next != null) { c = c.next; }
            tail = c;
        }

        return removed;
    }


    /**
     * returns the element at the given index
     *
     * @param index position to look at (starts at 0)
     * @return the element at that position
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        ListNode<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }


    /**
     * checks if the list contains the given item
     *
     * @param item the item to search for
     * @return true if found, false otherwise
     */
    public boolean contains(T item) {
        ListNode<T> current = head;
        while (current != null) {
            if (current.data.equals(item)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** returns how many items are in the list */
    public int size() { return size; }

    /** returns true if the list is empty */
    public boolean isEmpty() { return size == 0; }

    /** removes all items from the list */
    public void clear() { head = null; tail = null; size = 0; }


    // iterator so we can use this list in for-each loops
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private ListNode<T> current = head;
            public boolean hasNext() { return current != null; }
            public T next() {
                if (current == null) throw new NoSuchElementException();
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        ListNode<T> current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
