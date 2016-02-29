package wfly6280;

import java.util.List;

public interface ListHolder<K, V> extends Holder<K, V> {

    @Override void hold(K key, V value);

    @Override List<V> values(K key);
}