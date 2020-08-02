package org.imanity.framework.bukkit.hologram.api;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.RV;
import org.imanity.framework.bukkit.util.Utility;

import javax.annotation.Nullable;

public class PlaceholderViewHandler implements ViewHandler {

    private RV[] replaceValues;
    private ViewHandler viewHandler;

    public PlaceholderViewHandler(ViewHandler viewHandler, RV... replaceValues) {
        this.viewHandler = viewHandler;
        this.replaceValues = replaceValues;
    }

    @Override
    public String view(@Nullable Player player) {
        return Utility.replace(this.viewHandler.view(player), this.replaceValues);
    }
}
