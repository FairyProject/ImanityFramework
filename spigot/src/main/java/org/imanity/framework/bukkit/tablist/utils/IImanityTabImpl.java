package org.imanity.framework.bukkit.tablist.utils;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.tablist.ImanityTablist;

public interface IImanityTabImpl {

    default void removeSelf(Player player) {}

    TabEntry createFakePlayer(
            ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot
    );

    void updateFakeName(
            ImanityTablist imanityTablist, TabEntry tabEntry, String text
    );

    void updateFakeLatency(
            ImanityTablist imanityTablist, TabEntry tabEntry, Integer latency
    );

    void updateFakeSkin(
            ImanityTablist imanityTablist, TabEntry tabEntry, SkinTexture skinTexture
    );

    void updateHeaderAndFooter(
            ImanityTablist imanityTablist, String header, String footer
    );
}
