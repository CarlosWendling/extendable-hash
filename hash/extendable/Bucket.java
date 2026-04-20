package hash.extendable;

public class Bucket<K, V> {
    private Entry<K, V>[] slots;
    private int localDepth;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public Bucket(int capacity, int localDepth) {
        this.capacity = capacity;
        this.localDepth = localDepth;
        this.slots = new Entry[capacity];
    }

    public int getLocalDepth() { return localDepth; }
    public void setLocalDepth(int localDepth) { this.localDepth = localDepth; }

    public int getCount() {
        int count = 0;
        for (Entry<K, V> slot : slots) {
            if (slot != null && !slot.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    public boolean isFull() {
        return getCount() == capacity;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public Entry<K, V> getEntry(int index) {
        int count = 0;
        for (Entry<K, V> slot : slots) {
            if (slot != null && !slot.isDeleted()) {
                if (count == index) return slot;
                count++;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        int start = Math.abs(key.hashCode()) % capacity;
        int i = start;
        do {
            if (slots[i] == null) return false;
            if (!slots[i].isDeleted() && slots[i].getKey().equals(key)) return true;
            i = (i + 1) % capacity;
        } while (i != start);
        return false;
    }

    public V get(K key) {
        int start = Math.abs(key.hashCode()) % capacity;
        int i = start;
        do {
            if (slots[i] == null) return null;
            if (!slots[i].isDeleted() && slots[i].getKey().equals(key)) {
                return slots[i].getValue();
            }
            i = (i + 1) % capacity;
        } while (i != start);
        return null;
    }

    public V put(K key, V value) {
        int start = Math.abs(key.hashCode()) % capacity;
        int i = start;
        int firstDeleted = -1;

        do {
            if (slots[i] == null) {
                if (firstDeleted >= 0) {
                    slots[firstDeleted] = new Entry<>(key, value);
                } else {
                    slots[i] = new Entry<>(key, value);
                }
                return null;
            }

            if (slots[i].isDeleted()) {
                if (firstDeleted < 0) firstDeleted = i;
            } else if (slots[i].getKey().equals(key)) {
                V old = slots[i].getValue();
                slots[i].setValue(value);
                return old;
            }

            i = (i + 1) % capacity;
        } while (i != start);

        throw new IllegalStateException("Bucket cheio");
    }

    public V remove(K key) {
        int start = Math.abs(key.hashCode()) % capacity;
        int i = start;
        do {
            if (slots[i] == null) return null;
            if (!slots[i].isDeleted() && slots[i].getKey().equals(key)) {
                V value = slots[i].getValue();
                slots[i].setDeleted(true);
                return value;
            }
            i = (i + 1) % capacity;
        } while (i != start);
        return null;
    }

    public void splitInto(Bucket<K, V> newBucket, int discriminatorBit) {
        Entry<K, V>[] oldSlots = this.slots.clone();
        this.slots = new Entry[capacity];
        newBucket.slots = new Entry[capacity];

        for (Entry<K, V> entry : oldSlots) {
            if (entry != null && !entry.isDeleted()) {
                int bit = (entry.getKey().hashCode() >> discriminatorBit) & 1;
                if (bit == 0) {
                    insertIntoSlots(this.slots, entry);
                } else {
                    insertIntoSlots(newBucket.slots, entry);
                }
            }
        }
    }

    private void insertIntoSlots(Entry<K, V>[] slots, Entry<K, V> entry) {
        int start = Math.abs(entry.getKey().hashCode()) % capacity;
        int i = start;
        do {
            if (slots[i] == null) {
                slots[i] = entry;
                return;
            }
            i = (i + 1) % capacity;
        } while (i != start);
    }

    public void mergeFrom(Bucket<K, V> other) {
        for (Entry<K, V> entry : other.slots) {
            if (entry != null && !entry.isDeleted()) {
                insertIntoSlots(this.slots, entry);
            }
        }
        other.slots = new Entry[capacity];
    }
}