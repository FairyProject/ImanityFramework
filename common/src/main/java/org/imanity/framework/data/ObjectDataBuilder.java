package org.imanity.framework.data;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;

public class ObjectDataBuilder {

    private String name;
    private Class<? extends ObjectData> objectDataClass;

    public ObjectDataBuilder() {

    }

    public ObjectDataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ObjectDataBuilder dataClass(Class<? extends ObjectData> objectDataClass) {
        this.objectDataClass = objectDataClass;
        return this;
    }

    public void build() {
        StoreDatabase database = ImanityCommon.CORE_CONFIG.getDatabaseType(name).newDatabase();

        database.init(name, objectDataClass, StoreType.OBJECT);
    }
}
