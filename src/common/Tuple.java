package common;

public class Tuple<K, V> {

    private final K value1;
    private final V value2;

    public Tuple(K value1, V value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public K getValue1() {
        return value1;
    }

    public V getValue2() {
        return value2;
    }

}
