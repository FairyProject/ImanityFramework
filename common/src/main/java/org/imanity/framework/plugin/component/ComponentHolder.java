package org.imanity.framework.plugin.component;

import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class ComponentHolder {

    public Object newInstance(Class<?> type) {
        try {
            return type.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Class<?>[] type();

}
