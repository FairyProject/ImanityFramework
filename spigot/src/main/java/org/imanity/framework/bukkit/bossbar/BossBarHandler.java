package org.imanity.framework.bukkit.bossbar;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.Utility;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

public class BossBarHandler implements Runnable {

    public static final String METADATA = ImanityCommon.METADATA_PREFIX + "BossBar";

    private long tick;
    private BossBarAdapter adapter;

    public BossBarHandler(BossBarAdapter adapter, long tick) {

        this.adapter = adapter;
        this.tick = tick;

        iSpigot.INSTANCE.addMovementHandler(new MovementHandler() {
            @Override
            public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet) {
                if (to.getBlockX() == from.getBlockX()
                    && to.getBlockY() == from.getBlockY()
                    && to.getBlockZ() == from.getBlockZ()) {
                    return;
                }

                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }

            @Override
            public void handleUpdateRotation(Player player, Location to, Location from, PacketPlayInFlying packet) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                float deltaAngle = Math.abs(connection.lastYaw - to.getYaw()) + Math.abs(connection.lastPitch - to.getPitch());
                if (deltaAngle < 7.5F) {
                    return;
                }

                BossBar bossBar = getOrCreate(player);
                bossBar.getMoved().set(true);
            }
        });

        Imanity.registerEvents(new Listener() {

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();

                BossBar bossBar = getOrCreate(player);
                bossBar.destroy(player);

                player.removeMetadata(BossBarHandler.METADATA, Imanity.PLUGIN);
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
                Thread.sleep(50L * tick);
            } catch (InterruptedException e) {
                throw new RuntimeException("Something wrong while ticking boss bar", e);
            }

        }

        Thread.interrupted();
    }

    private void tick() {

        for (Player player : Imanity.PLUGIN.getServer().getOnlinePlayers()) {

            BossBar bossBar = this.getOrCreate(player);
            BossBarData bossBarData = this.adapter.tick(bossBar);

            if (bossBarData.getHealth() <= 0.0F) {
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
        if (player.hasMetadata(BossBarHandler.METADATA)) {
            return (BossBar) player.getMetadata(BossBarHandler.METADATA).get(0).value();
        }
        BossBar bossBar = new BossBar(player);
        player.setMetadata(BossBarHandler.METADATA, new SampleMetadata(bossBar));
        return bossBar;
    }

}
