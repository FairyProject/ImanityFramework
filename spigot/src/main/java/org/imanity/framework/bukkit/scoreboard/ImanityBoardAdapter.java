package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface ImanityBoardAdapter {

    default void onBoardCreate(Player player, ImanityBoard board) {

    }


    String getTitle(Player player);

    List<String> getLines(Player player);

    default int tick() {
        return 2;
    }

}
