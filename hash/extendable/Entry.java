package hash.extendable;

public class Entry<K, V> {
    private final K key;
    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;
        Entry<?, ?> e = (Entry<?, ?>) o;
        return key.equals(e.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}