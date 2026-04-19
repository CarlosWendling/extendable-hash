package hash.extendable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Hashtable<K,V> {
    private List<List<Entry<K,V>>> buckets;

    public Hashtable(){
        this(16);
    }

    public Hashtable(int s){
        this.buckets = new ArrayList<>(s);
        for (int x = 0; x < s; x++){
            buckets.add(null);
        }
    }

    public V put(K key, V value) {
        throwIfNull(key);

        var entry = this.findOrCreateEntry(key);
        var oldValue = entry.getValue();
        entry.setValue(value);

        return oldValue;
    }


    private int bucketIndeFor(K key){
        return key .hashCode() % this.buckets.size();
    }


    private Entry<K,V> findOrCreateEntry(K key){
        var bucketIndex = this.bucketIndeFor(key);
        var bucket = this.buckets.get(bucketIndex);

        if (bucket == null) {
            bucket = new ArrayList<>();
            this.buckets.set(bucketIndex, bucket);
        }

        for (var entry : bucket) {
            if (key.equals(entry.getKey())) {
                return entry;
            }
        }

        var entry = new Entry<K,V>(key);
        bucket.add(entry);
        return entry;
    }

    static void throwIfNull(Object key){
        if (key == null){
            throw  new NullPointerException("key must not be null");
        }
    }
}
