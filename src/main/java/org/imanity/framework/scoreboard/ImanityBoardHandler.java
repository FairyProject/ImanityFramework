package org.imanity.framework.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.imanity.framework.Imanity;
import org.imanity.framework.util.SampleMetadata;
import org.imanity.framework.util.Utility;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImanityBoardHandler implements Runnable {

    private ImanityBoardAdapter adapter = null;

    public ImanityBoardHandler(ImanityBoardAdapter adapter) {
        this.adapter = adapter;

        Thread thread = new Thread(this);
        thread.setName("Imanity Scoreboard Thread");
        thread.setDaemon(true);
        thread.start();

        Imanity.PLUGIN.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();

                remove(player);
            }

        }, Imanity.PLUGIN);
    }

    @Override
    public void run() {
        while (!Imanity.SHUTTING_DOWN) {
            try {
                this.tick();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Thread.sleep(2 * 50L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void tick() {
        for (Player player : Imanity.PLUGIN.getServer().getOnlinePlayers()) {

            if (Imanity.SHUTTING_DOWN) {
                break;
            }

            ImanityBoard board = this.getOrCreateScoreboard(player);

            String title = Utility.color(adapter.getTitle(player));

            board.setTitle(title);

            List<String> newLines = this.adapter.getLines(player);

            if (newLines == null || newLines.isEmpty()) {
                board.remove();
            } else {

                board.setLines(newLines);

            }
        }
    }

    public void remove(Player player) {
        ImanityBoard board = this.get(player);

        if (board != null) {
            board.remove();
            player.removeMetadata(ImanityBoard.METADATA_TAG, Imanity.PLUGIN);
        }
    }

    public ImanityBoard get(Player player) {
        if (player.hasMetadata(ImanityBoard.METADATA_TAG)) {
            return (ImanityBoard) player.getMetadata(ImanityBoard.METADATA_TAG).get(0).value();
        }
        return null;
    }

    public ImanityBoard getOrCreateScoreboard(Player player) {
        ImanityBoard board = this.get(player);

        if (board == null) {
            board = new ImanityBoard(player);
            player.setMetadata(ImanityBoard.METADATA_TAG, new SampleMetadata(board));
        }
        return board;
    }


}
