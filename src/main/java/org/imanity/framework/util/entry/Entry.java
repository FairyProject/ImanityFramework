package org.imanity.framework.util.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entry<K, V> {

    private K key;
    private V value;

}
