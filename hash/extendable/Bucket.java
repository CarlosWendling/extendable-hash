package hash.extendable;

public class Bucket<K, V> {
    private Entry<K, V>[] slots;
    private int localDepth;
    private int count;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public Bucket(int capacity, int localDepth) {
        this.capacity = capacity;
        this.localDepth = localDepth;
        this.count = 0;
        this.slots = new Entry[capacity];
    }

    public int getLocalDepth() { return localDepth; }
    public void setLocalDepth(int localDepth) { this.localDepth = localDepth; }
    public int getCount() { return count; }
    public boolean isFull() { return count == capacity; }
    public boolean isEmpty() { return count == 0; }
    public Entry<K, V> getEntry(int index) {
        if (index >= 0 && index < count) return slots[index];
        return null;
    }

    public boolean containsKey(K key) {
        for (int i = 0; i < count; i++) {
            if (slots[i].getKey().equals(key)) return true;
        }
        return false;
    }

    public V get(K key) {
        for (int i = 0; i < count; i++) {
            if (slots[i].getKey().equals(key)) {
                return slots[i].getValue();
            }
        }
        return null;
    }

    public V put(K key, V value) {
        // Tenta atualizar se ja existe
        for (int i = 0; i < count; i++) {
            if (slots[i].getKey().equals(key)) {
                V old = slots[i].getValue();
                slots[i].setValue(value);
                return old;
            }
        }
        // Insere em slot vazio (sondagem linear dentro do bucket)
        if (count < capacity) {
            slots[count] = new Entry<>(key, value);
            count++;
            return null;
        }
        throw new IllegalStateException("Bucket cheio");
    }

    public V remove(K key) {
        for (int i = 0; i < count; i++) {
            if (slots[i].getKey().equals(key)) {
                V value = slots[i].getValue();
                // Desloca elementos para fechar o espaco
                for (int j = i; j < count - 1; j++) {
                    slots[j] = slots[j + 1];
                }
                slots[count - 1] = null;
                count--;
                return value;
            }
        }
        return null;
    }

    public void splitInto(Bucket<K, V> newBucket, int discriminatorBit) {
        newBucket.count = 0;
        int oldCount = this.count;
        Entry<K, V>[] oldSlots = this.slots.clone();
        this.count = 0;
        for (int i = 0; i < this.slots.length; i++) this.slots[i] = null;

        for (int i = 0; i < oldCount; i++) {
            Entry<K, V> entry = oldSlots[i];
            int bit = (entry.getKey().hashCode() >> discriminatorBit) & 1;
            if (bit == 0) {
                this.slots[this.count++] = entry;
            } else {
                newBucket.slots[newBucket.count++] = entry;
            }
        }
    }

    public void mergeFrom(Bucket<K, V> other) {
        for (int i = 0; i < other.count; i++) {
            this.slots[this.count++] = other.slots[i];
        }
        other.count = 0;
    }
}