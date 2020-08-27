package org.imanity.framework.bukkit.zigguart;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.zigguart.utils.BufferedTabObject;

import java.util.Set;

public interface ImanityTabAdapter {

    Set<BufferedTabObject> getSlots(Player player);

    String getFooter();

    String getHeader();

}
