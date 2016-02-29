package wfly6280;

import java.util.List;

/** Package protected to have public methods bridged in public subclasses */
class Parent<K, V> extends Grandparent<K, V> implements ListHolder<K, V> {

    public String methodToCall() {
        return "value";
    }

    @Override public void hold(K key, V value) {
    }

    @Override public List<V> values(K key) {
        return null;
    }
}