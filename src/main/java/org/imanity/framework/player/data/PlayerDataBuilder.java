package org.imanity.framework.player.data;

import org.imanity.framework.Imanity;
import org.imanity.framework.player.data.store.StoreDatabase;

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
        StoreDatabase database = Imanity.DATA_CONFIG.getDatabaseType(name).newDatabase();

        database.setAutoLoad(this.loadOnJoin);
        database.setAutoSave(this.saveOnQuit);

        database.init(name, playerDataClass);

        PlayerData.DATABASES.put(playerDataClass, database);
    }
}
