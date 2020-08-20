package org.imanity.framework.data.store;

import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.ObjectData;
import org.imanity.framework.data.PlayerData;

public enum StoreType {

    PLAYER(PlayerData.class),
    OBJECT(ObjectData.class);

    private Class<? extends AbstractData> dataClass;

    private StoreType(Class<? extends AbstractData> dataClass) {
        this.dataClass = dataClass;
    }

}
