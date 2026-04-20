package hash.extendable;

public class Entry<K, V> {
    private final K key;
    private V value;
    private boolean deleted;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
        this.deleted = false;
    }

    public Entry(K key, V value, boolean deleted) {
        this.key = key;
        this.value = value;
        this.deleted = deleted;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

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