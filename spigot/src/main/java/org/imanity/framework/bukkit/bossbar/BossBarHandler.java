package org.imanity.framework.bukkit.bossbar;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.util.Utility;

public class BossBarHandler implements Runnable {

    public static final MetadataKey<BossBar> METADATA = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "BossBar", BossBar.class);

    private static final long v1_7_tick = 3L;
    private static final long v1_8_tick = 60L;

    private BossBarAdapter adapter;

    public BossBarHandler(BossBarAdapter adapter) {

        this.adapter = adapter;

        Imanity.registerMovementListener(new MovementListener() {
            @Override
            public void handleUpdateLocation(Player player, Location from, Location to) {
                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }

            @Override
            public void handleUpdateRotation(Player player, Location from, Location to) {
                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }
        })
                .ignoreSameBlock()
                .register();

        Imanity.registerEvents(new Listener() {

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();

                BossBar bossBar = getOrCreate(player);
                bossBar.destroy(player);

                Metadata
                        .provideForPlayer(player)
                        .remove(METADATA);
            }

            @EventHandler
            public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
                Player player = event.getPlayer();

                BossBar bossBar = getOrCreate(player);
                bossBar.destroy(player);
            }

        });

        Thread thread = new Thread(this);
        thread.setName("Imanity Boss Bar Thread");
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void run() {
        while (!Imanity.SHUTTING_DOWN) {

            try {
                this.tick();
            } catch (Throwable throwable) {
                throw new RuntimeException("Something wrong while ticking boss bar", throwable);
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                throw new RuntimeException("Something wrong while ticking boss bar", e);
            }

        }

        Thread.interrupted();
    }

    private long getUpdateTick(BossBar bossBar) {
        switch (bossBar.getVersion()) {
            case v1_7:
                return v1_7_tick * 50L;
            default:
                return v1_8_tick * 50L;
        }
    }

    private void tick() {

        long now = System.currentTimeMillis();

        for (Player player : Imanity.getPlayers()) {

            BossBar bossBar = this.getOrCreate(player);

            if (now - bossBar.getLastUpdate() < this.getUpdateTick(bossBar)) {
                continue;
            }

            bossBar.setLastUpdate(now);
            BossBarData bossBarData = this.adapter.tick(bossBar);

            if (bossBarData == null || bossBarData.getHealth() <= 0.0F) {
                bossBar.destroy(player);
                continue;
            }

            if (bossBarData.getText() == null) {
                bossBarData.setText("");
            }

            bossBarData.setText(Utility.color(bossBarData.getText()));
            bossBar.send(bossBarData);

        }

    }

    public BossBar getOrCreate(Player player) {
        return Metadata
                .provideForPlayer(player)
                .getOrPut(METADATA, () -> new BossBar(player));
    }

}
