package datastructures;


// a LIFO stack built using linked nodes
// used in dijkstra to reconstruct the shortest path in the right order
public class Stack<T> {

    // inner node that holds the data and a pointer to the one below it
    private static class StackNode<T> {
        T data;
        StackNode<T> next;
        StackNode(T data) { this.data = data; this.next = null; }
    }

    private StackNode<T> top;
    private int size;

    /** creates an empty stack */
    public Stack() { top = null; size = 0; }


    /**
     * pushes an item onto the top of the stack
     *
     * @param item the item to push
     */
    public void push(T item) {
        StackNode<T> newNode = new StackNode<>(item);
        newNode.next = top; // new node sits on top of the old one
        top = newNode;
        size++;
    }


    /**
     * removes and returns the item on top of the stack
     *
     * @return the top item
     * @throws IllegalStateException if the stack is empty
     */
    public T pop() {
        if (top == null) throw new IllegalStateException("Stack is empty can not remove");
        T data = top.data;
        top = top.next; // move top down to the next node
        size--;
        return data;
    }

    /**
     * shows the top item without removing it
     *
     * @return the item at the top
     */
    public T peek() {
        if (top == null) throw new IllegalStateException("Stack is empty can not see top");
        return top.data;
    }

    /** returns true if there are no items in the stack */
    public boolean isEmpty() { return top == null; }

    /** returns how many items are in the stack */
    public int size() { return size; }

    /** removes all items from the stack */
    public void clear() { top = null; size = 0; }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack[top->");
        StackNode<T> current = top;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) sb.append("->");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
