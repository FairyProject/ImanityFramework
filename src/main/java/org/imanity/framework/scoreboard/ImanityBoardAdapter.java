package org.imanity.framework.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface ImanityBoardAdapter {

    String getTitle(Player player);

    List<String> getLines(Player player);

}
