package org.imanity.framework.scoreboard.impl;

import org.bukkit.entity.Player;
import org.imanity.framework.scoreboard.ImanityBoardAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExampleBoardAdapter implements ImanityBoardAdapter {
    @Override
    public String getTitle(Player player) {
        return "hi! &6BRUH";
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            lines.add(i + " &eBRUH");
        }
        return lines;
    }
}
