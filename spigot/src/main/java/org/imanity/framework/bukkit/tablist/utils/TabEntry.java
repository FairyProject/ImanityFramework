package org.imanity.framework.bukkit.tablist.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.imanity.framework.bukkit.tablist.ImanityTablist;

@Getter @Setter @AllArgsConstructor
public class TabEntry {

    private String id;
    private OfflinePlayer offlinePlayer;
    private String text;
    private ImanityTablist tab;
    private SkinTexture texture;
    private TabColumn column;
    private int slot;
    private int rawSlot;
    private int latency;

}
