package datastructures;


// custom hash map using separate chaining to handle collisions
// we use this for graph storage instead of java's built-in map
public class HashMap<K, V> {

    // each entry stores a key, value, and a pointer to the next in the chain
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;
        Entry(K key, V value) { this.key = key; this.value = value; this.next = null; }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private Entry<K, V>[] buckets;
    private int size;


    /** creates a hash map with default capacity of 16 */
    @SuppressWarnings("unchecked")
    public HashMap() { buckets = new Entry[DEFAULT_CAPACITY]; size = 0; }

    /**
     * creates a hash map with a specific starting capacity
     *
     * @param initialCapacity how many buckets to start with
     */
    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = DEFAULT_CAPACITY;
        }
        buckets = new Entry[initialCapacity];
        size = 0;
    }


    /**
     * stores a key-value pair in the map
     * if the key already exists its value gets replaced
     *
     * @param key the key to store
     * @param value the value to associate with the key
     */
    public void put(K key, V value) {
        if (key == null) return;
        if ((double) size / buckets.length > LOAD_FACTOR) {
            resize();
        }

        int index = bucketIndex(key);
        Entry<K, V> current = buckets[index];

        // check if the key already exists in this bucket
        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value; // update existing value
                return;
            }
            current = current.next;
        }

        // key not found so add a new entry at the front of the chain
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
    }


    /**
     * only adds the key-value pair if the key isn't already in the map
     *
     * @param key the key to check
     * @param value the value to add if key is new
     */
    public void putIfAbsent(K key, V value) {
        if (!containsKey(key)) {
            put(key, value);
        }
    }


    /**
     * looks up a value by its key
     *
     * @param key the key to search for
     * @return the value, or null if the key doesn't exist
     */
    public V get(K key) {
        if (key == null) return null;
        int index = bucketIndex(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }


    /**
     * gets the value for a key, but returns a default if key doesn't exist
     *
     * @param key the key to look up
     * @param defaultValue what to return if key is not found
     * @return the value or the default
     */
    public V getOrDefault(K key, V defaultValue) {
        V val = get(key);
        return val != null ? val : defaultValue;
    }


    /**
     * checks whether a key exists in the map
     *
     * @param key the key to check for
     * @return true if found, false otherwise
     */
    public boolean containsKey(K key) {
        if (key == null) return false;
        int index = bucketIndex(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return true;
            }
            current = current.next;
        }

        return false;
    }


    /**
     * removes an entry from the map by key
     *
     * @param key the key of the entry to remove
     * @return the value that was removed, or null if key wasn't found
     */
    public V remove(K key) {
        if (key == null) return null;
        int index = bucketIndex(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> previous = null;

        while (current != null) {
            
            if (current.key.equals(key)) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }

        return null;
    }


    /**
     * returns all values stored in the map as a linked list
     *
     * @return a LinkedList containing every value
     */
    public LinkedList<V> values() {
        LinkedList<V> result = new LinkedList<>();
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> current = bucket;
            while (current != null) {
                result.add(current.value);
                current = current.next;
            }
        }
        return result;
    }


    /**
     * returns all keys stored in the map as a linked list
     *
     * @return a LinkedList containing every key
     */
    public LinkedList<K> keys() {
        LinkedList<K> result = new LinkedList<>();
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> current = bucket;
            while (current != null) {
                result.add(current.key);
                current = current.next;
            }
        }
        return result;
    }

    /** returns how many entries are in the map */
    public int size() { return size; }

    /** returns true if the map has no entries */
    public boolean isEmpty() { return size == 0; }

    /** removes all entries from the map */
    @SuppressWarnings("unchecked")
    public void clear() { buckets = new Entry[buckets.length]; size = 0; }


    // hashes the key and maps it to a bucket index
    private int bucketIndex(K key) { return Math.abs(key.hashCode()) % buckets.length; }

    // doubles the bucket array size and re-inserts everything
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldBuckets = buckets;
        buckets = new Entry[oldBuckets.length * 2];
        size = 0;

        for (Entry<K, V> bucket : oldBuckets) {
            Entry<K, V> current = bucket;
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }

    @Override
    public String toString() { return "HashMap[size=" + size + ", buckets=" + buckets.length + "]"; }
}
