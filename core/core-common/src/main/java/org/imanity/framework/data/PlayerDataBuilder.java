/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
