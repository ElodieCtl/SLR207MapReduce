package src.slave;

import java.io.Serializable;

public class Pair<K,V> implements Serializable {

    private static final long serialVersionUID = 1L;

    public final K key;
    public final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + this.key + ", " + this.value + ")";
    }
    
}
