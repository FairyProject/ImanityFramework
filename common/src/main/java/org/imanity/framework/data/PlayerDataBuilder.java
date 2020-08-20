package org.imanity.framework.data;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;

public class PlayerDataBuilder {

    private String name;
    private Class<? extends PlayerData> playerDataClass;

    private boolean loadOnJoin;
    private boolean saveOnQuit;

    public PlayerDataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PlayerDataBuilder playerDataClass(Class<? extends PlayerData> playerDataClass) {
        this.playerDataClass = playerDataClass;
        return this;
    }

    public PlayerDataBuilder loadOnJoin(boolean loadOnJoin) {
        this.loadOnJoin = loadOnJoin;
        return this;
    }

    public PlayerDataBuilder saveOnQuit(boolean saveOnQuit) {
        this.saveOnQuit = saveOnQuit;
        return this;
    }

    public void build() {
        StoreDatabase database = ImanityCommon.CORE_CONFIG.getDatabaseType(name).newDatabase();

        database.setAutoLoad(this.loadOnJoin);
        database.setAutoSave(this.saveOnQuit);

        database.init(name, playerDataClass, StoreType.PLAYER);
    }
}
