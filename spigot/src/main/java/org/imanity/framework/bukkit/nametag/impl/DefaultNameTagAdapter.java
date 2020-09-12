package org.imanity.framework.bukkit.nametag.impl;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.nametag.NameTagAdapter;
import org.imanity.framework.bukkit.nametag.NameTagInfo;

public class DefaultNameTagAdapter extends NameTagAdapter {
    public DefaultNameTagAdapter() {
        super("Default", 0);
    }

    @Override
    public NameTagInfo fetch(Player receiver, Player target) {
        return createNametag("", "");
    }
}
