package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.metadata.MetadataKey;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.bukkit.util.Utility;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImanityBoardHandler implements Runnable {

    private final ImanityBoardAdapter adapter;
    private final Queue<Runnable> runnables = new ConcurrentLinkedQueue<>();

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

            ImanityBoard board = this.get(player);
            if (board == null) {
                continue;
            }

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
        ImanityBoard board = this.getOrCreateScoreboard(player);

        board.setTagUpdated(true);

        for (Player other : Imanity.PLUGIN.getServer().getOnlinePlayers()) {
            ImanityBoard otherBoard = this.getOrCreateScoreboard(other);

            otherBoard.updatePlayer(player);

            if (doSelf) {
                board.updatePlayer(other);
            }
        }
    }

    public void remove(Player player) {
        ImanityBoard board = this.get(player);

        if (board != null) {
            board.remove();
            Metadata.provideForPlayer(player).remove(ImanityBoard.METADATA_TAG);

            String name = player.getName();

            if (board.isTagUpdated()) {
                this.runnables.add(() -> {
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        if (player == other) {
                            continue;
                        }

                        ImanityBoard otherBoard = this.getOrCreateScoreboard(other);
                        otherBoard.removePlayer(name);
                    }
                });
            }
        }
    }

    public ImanityBoard get(Player player) {
        return Metadata.provideForPlayer(player).getOrNull(ImanityBoard.METADATA_TAG);
    }

    public ImanityBoard getOrCreateScoreboard(Player player) {
        return Metadata.provideForPlayer(player).getOrPut(ImanityBoard.METADATA_TAG, () -> {
            ImanityBoard board = new ImanityBoard(player, this.adapter);
            this.adapter.onBoardCreate(player, board);
            return board;
        });
    }


}
