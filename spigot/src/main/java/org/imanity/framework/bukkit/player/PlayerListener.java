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
import org.imanity.framework.util.thread.ServerThreadLock;

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

                        PlayerData playerData = (PlayerData) database.getByUuid(player.getUniqueId());

                        if (playerData == null) {
                            playerData = (PlayerData) database.load(player);
                        }

                        PlayerData finalPlayerData = playerData;
                        try (ServerThreadLock lock = ServerThreadLock.obtain()) {
                            player.setMetadata(database.getMetadataTag(), new SampleMetadata(finalPlayerData));

                            PlayerDataLoadEvent.callEvent(player, finalPlayerData);
                        }
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
                });

            try (ServerThreadLock lock = ServerThreadLock.obtain()) {
                DataHandler.getPlayerDatabases()
                        .forEach(database -> {
                            player.removeMetadata(database.getMetadataTag(), Imanity.PLUGIN);

                            if (!database.autoSave()) {
                                return;
                            }
                            database.delete(player.getUniqueId());
                        });
            }
        };

        if (ImanityCommon.CORE_CONFIG.ASYNCHRONOUS_DATA_STORING) {
            TaskUtil.runAsync(runnable);
        } else {
            runnable.run();
        }
    }

}
