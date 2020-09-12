package org.imanity.framework.bukkit.nametag;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class NameTagList {

    private Map<String, NameTagInfo> infos = new HashMap<>();

    @Nullable
    public NameTagInfo getTeamFor(String name) {
        return this.infos.getOrDefault(name, null);
    }

    public void putTeamFor(String name, NameTagInfo tagInfo) {
        this.infos.put(name, tagInfo);
    }

    public void removeTeamFor(String name) {
        this.infos.remove(name);
    }

}
