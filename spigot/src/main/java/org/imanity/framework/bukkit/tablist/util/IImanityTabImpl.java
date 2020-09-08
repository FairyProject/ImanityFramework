package org.imanity.framework.bukkit.tablist.util;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.util.Skin;

public interface IImanityTabImpl {

    default void removeSelf(Player player) {}

    default void registerLoginListener() {}

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
            ImanityTablist imanityTablist, TabEntry tabEntry, Skin skin
    );

    void updateHeaderAndFooter(
            ImanityTablist imanityTablist, String header, String footer
    );
}
