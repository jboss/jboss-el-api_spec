package wfly6280;

import java.util.Collection;

public interface Holder<K, V> {

    void hold(K key, V value);

    Collection<V> values(K v);
}