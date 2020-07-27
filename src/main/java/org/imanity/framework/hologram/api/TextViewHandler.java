package org.imanity.framework.hologram.api;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.imanity.framework.util.Utility;

import javax.annotation.Nullable;

@AllArgsConstructor
public class TextViewHandler implements ViewHandler {

    private final String text;

    @Override
    public String view(@Nullable Player player) {
        return Utility.color(text);
    }
}
