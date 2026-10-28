package datastructures;


// binary min-heap using parallel arrays for priorities and values
// always gives back the entry with the smallest priority first
// we use this in dijkstra as the priority queue
public class Heap {

    private double[] priorities;
    private int[] values;
    private int size; // how many entries are currently in the heap


    /**
     * creates a heap with the given initial array capacity
     *
     * @param initialCapacity how many entries to allocate space for initially
     */
    public Heap(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = 16;
        }
        this.priorities = new double[initialCapacity];
        this.values = new int[initialCapacity];
        this.size = 0;
    }

    /** creates a heap with default capacity of 16 */
    public Heap() { this(16); }


    /**
     * inserts a new entry into the heap and moves it up to the right position
     *
     * @param priority the priority value (lower = higher priority)
     * @param value the value to store with this priority
     */
    public void insert(double priority, int value) {
        ensureCapacity(); // grow arrays if we're running out of space
        priorities[size] = priority;
        values[size] = value;
        siftUp(size);
        size++;
    }


    /**
     * removes and returns the entry with the smallest priority
     *
     * @return an array where [0] is the priority and [1] is the value
     * @throws IllegalStateException if the heap is empty
     */
    public double[] extractMin() {
        if (size == 0) throw new IllegalStateException("Heap is empty");

        // grab the min values before we overwrite them
        double minPriority = priorities[0];
        int minValue = values[0];

        size--;

        // move the last element to the top and sift it down
        priorities[0] = priorities[size];
        values[0] = values[size];

        if (size > 0) {
            siftDown(0);
        }

        return new double[]{minPriority, minValue};
    }

    /**
     * shows the top entry without removing it
     *
     * @return array with [0] = priority and [1] = value
     */
    public double[] peek() {
        if (size == 0) throw new IllegalStateException("Heap is empty");
        return new double[]{priorities[0], values[0]};
    }

    /** returns true if the heap has no entries */
    public boolean isEmpty() { return size == 0; }

    /** returns how many entries are in the heap */
    public int size() { return size; }


    // moves an element up the heap until it's in the right place
    private void siftUp(int index) {
        while (index > 0) {
            int parentIdx = (index - 1) / 2; // parent is always at (i-1)/2
            if (priorities[index] < priorities[parentIdx]) {
                swap(index, parentIdx);
                index = parentIdx;
            } else {
                break;
            }
        }
    }

    // moves an element down the heap until it's in the right place
    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;  // left child index
            int right = 2 * index + 2; // right child index
            int smallest = index;

            // check if either child has a smaller priority
            if (left < size && priorities[left] < priorities[smallest]) {
                smallest = left;
            }
            if (right < size && priorities[right] < priorities[smallest]) {
                smallest = right;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break; // already in the right spot
            }
        }
    }

    // swaps two entries in both parallel arrays at the same time
    private void swap(int i, int j) {
        double tmpP = priorities[i];
        priorities[i] = priorities[j];
        priorities[j] = tmpP;

        int tmpV = values[i];
        values[i] = values[j];
        values[j] = tmpV;
    }

    // doubles the array size when we run out of space
    private void ensureCapacity() {
        if (size >= priorities.length) {
            int newCap = priorities.length * 2;
            double[] newP = new double[newCap];
            int[] newV = new int[newCap];
            System.arraycopy(priorities, 0, newP, 0, size);
            System.arraycopy(values, 0, newV, 0, size);
            priorities = newP;
            values = newV;
        }
    }

    @Override
    public String toString() { return "Heap[size=" + size + "]"; }
}
