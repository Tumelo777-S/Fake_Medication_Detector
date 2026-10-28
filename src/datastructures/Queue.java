package datastructures;


// a basic FIFO queue built with linked nodes
// we use this in BFS to process nodes level by level
public class Queue<T> {

    // inner node that holds the data and points to the next one
    private static class QueueNode<T> {
        T data;
        QueueNode<T> next;
        QueueNode(T data) { this.data = data; this.next = null; }
    }

    private QueueNode<T> front;
    private QueueNode<T> rear;
    private int size;

    /** creates an empty queue */
    public Queue() { front = null; rear = null; size = 0; }


    /**
     * adds an item to the back of the queue
     *
     * @param item the item to add
     */
    public void enqueue(T item) {
        QueueNode<T> newNode = new QueueNode<>(item);
        if (rear != null) {
            rear.next = newNode;
        }
        rear = newNode;
        if (front == null) {
            front = newNode;
        }
        size++;
    }


    /**
     * removes and returns the item at the front of the queue
     *
     * @return the item that was at the front
     * @throws IllegalStateException if the queue is empty
     */
    public T dequeue() {
        if (front == null) throw new IllegalStateException("Queue is empty can not remove");
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null; // queue is now empty so rear must also be null
        }
        size--;
        return data;
    }

    /**
     * looks at the front item without removing it
     *
     * @return the front item
     */
    public T peek() {
        if (front == null) throw new IllegalStateException("Queue is empty");
        return front.data;
    }

    /** returns true if the queue has no items in it */
    public boolean isEmpty() { return front == null; }

    /** returns how many items are in the queue */
    public int size() { return size; }

    /** removes all items from the queue */
    public void clear() { front = null; rear = null; size = 0; }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Queue[front->");
        QueueNode<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append("->");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
