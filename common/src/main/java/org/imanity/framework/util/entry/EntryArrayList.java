package org.imanity.framework.util.entry;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class EntryArrayList<K, V> extends ArrayList<Entry<K, V>> {

    public void add(K k, V v) {
        this.add(new Entry<>(k, v));
    }

    public boolean remove(K k, V v) {
        return this.remove(new Entry<>(k, v));
    }

    public boolean removeIf(BiPredicate<K, V> filter) {
        return this.removeIf(entry -> filter.test(entry.getKey(), entry.getValue()));
    }

    public void forEach(BiConsumer<K, V> consumer) {
        this.forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }
}
