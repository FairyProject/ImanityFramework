package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.Utility;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImanityBoardHandler implements Runnable {

    private ImanityBoardAdapter adapter = null;
    private Queue<Runnable> runnables = new ConcurrentLinkedQueue<>();

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

    public void runQueue() {
        Runnable runnable;
        while ((runnable = this.runnables.poll()) != null) {
            runnable.run();
        }
    }

    @Override
    public void run() {
        while (!Imanity.SHUTTING_DOWN) {
            try {
                this.tick();

                this.runQueue();
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

    public void updatePlayer(Player player, boolean doSelf) {
        this.runnables.add(() -> {
            if (!player.isOnline()) {
                return;
            }

            ImanityBoard board = this.getOrCreateScoreboard(player);

            board.setTagUpdated(true);

            for (Player other : Imanity.PLUGIN.getServer().getOnlinePlayers()) {
                ImanityBoard otherBoard = this.getOrCreateScoreboard(other);

                otherBoard.updatePlayer(player);

                if (doSelf) {
                    board.updatePlayer(other);
                }
            }
        });
    }

    public void remove(Player player) {
        ImanityBoard board = this.get(player);

        if (board != null) {
            board.remove();
            player.removeMetadata(ImanityBoard.METADATA_TAG, Imanity.PLUGIN);

            if (board.isTagUpdated()) {
                this.runnables.add(() -> {
                    for (Player other : Bukkit.getOnlinePlayers()) {

                        ImanityBoard otherBoard = this.getOrCreateScoreboard(other);
                        otherBoard.removePlayer(player);

                    }
                });
            }
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
            board = new ImanityBoard(player, this.adapter);
            player.setMetadata(ImanityBoard.METADATA_TAG, new SampleMetadata(board));

            this.adapter.onBoardCreate(player, board);
        }
        return board;
    }


}
