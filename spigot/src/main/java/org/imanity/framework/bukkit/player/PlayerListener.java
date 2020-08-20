package org.imanity.framework.bukkit.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.player.event.PlayerDataLoadEvent;
import org.imanity.framework.data.DataHandler;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.TaskUtil;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        Runnable runnable = () -> {
            DataHandler.getPlayerDatabases()
                    .forEach(database -> {
                        if (!database.autoLoad()) {
                            return;
                        }
                        PlayerData playerData = (PlayerData) database.load(player);
                        player.setMetadata(database.getMetadataTag(), new SampleMetadata(playerData));

                        PlayerDataLoadEvent.callEvent(player, playerData);
                    });
        };

        if (ImanityCommon.CORE_CONFIG.ASYNCHRONOUS_DATA_STORING) {
            TaskUtil.runAsync(runnable);
        } else {
            runnable.run();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        Runnable runnable = () -> {
            DataHandler.getPlayerDatabases()
                .forEach(database -> {
                    if (!database.autoSave()) {
                        return;
                    }
                    PlayerData playerData = database.getByPlayer(player);
                    database.save(playerData);
                    database.delete(player.getUniqueId());
                });

            Runnable finalRunnable = () -> {
                DataHandler.getPlayerDatabases()
                        .forEach(database -> player.removeMetadata(database.getMetadataTag(), Imanity.PLUGIN));
            };

            if (!SpigotUtil.isServerThread()) {
                TaskUtil.runSync(finalRunnable);
            } else {
                finalRunnable.run();
            }
        };

        if (ImanityCommon.CORE_CONFIG.ASYNCHRONOUS_DATA_STORING) {
            TaskUtil.runAsync(runnable);
        } else {
            runnable.run();
        }
    }

}
