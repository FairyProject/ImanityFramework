package org.imanity.framework.bukkit.tablist;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.tablist.utils.BufferedTabObject;

import java.util.Set;

public interface ImanityTabAdapter {

    Set<BufferedTabObject> getSlots(Player player);

    String getFooter(Player player);

    String getHeader(Player player);

}
