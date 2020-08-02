package org.imanity.framework.util.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entry<K, V> {

    private K key;
    private V value;

    public Document toDocument() {
        Document document = new Document();
        document.put("key", key);
        document.put("value", value);
        return document;
    }

}
