package org.imanity.framework.player.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.Imanity;
import org.imanity.framework.util.SampleMetadata;
import org.imanity.framework.util.SpigotUtil;
import org.imanity.framework.util.TaskUtil;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        Runnable runnable = () -> {
            PlayerData.getStoreDatabases()
                    .forEach(database -> {
                        PlayerData playerData = database.load(player);
                        player.setMetadata(database.getMetadataTag(), new SampleMetadata(playerData));
                    });
        };

        if (Imanity.DATA_CONFIG.ASYNCHRONOUS_DATA_STORING) {
            TaskUtil.runAsync(runnable);
        } else {
            TaskUtil.runSync(runnable);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        Runnable runnable = () -> {
            PlayerData.getStoreDatabases()
                .forEach(database -> {
                    PlayerData playerData = database.getByPlayer(player);
                    database.save(playerData);
                });

            Runnable finalRunnable = () -> {
                PlayerData
                        .getStoreDatabases()
                        .forEach(database -> player.removeMetadata(database.getMetadataTag(), Imanity.PLUGIN));
            };

            if (!SpigotUtil.isServerThread()) {
                TaskUtil.runSync(finalRunnable);
            } else {
                finalRunnable.run();
            }
        };

        if (Imanity.DATA_CONFIG.ASYNCHRONOUS_DATA_STORING) {
            TaskUtil.runAsync(runnable);
        } else {
            TaskUtil.runSync(runnable);
        }
    }

}
