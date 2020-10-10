package org.imanity.framework.bungee.impl;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.imanity.framework.player.IPlayerBridge;

import java.util.Collection;
import java.util.UUID;

public class BungeePlayerBridge implements IPlayerBridge<ProxiedPlayer> {
    @Override
    public Collection<? extends ProxiedPlayer> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers();
    }

    @Override
    public UUID getUUID(ProxiedPlayer proxiedPlayer) {
        return proxiedPlayer.getUniqueId();
    }

    @Override
    public String getName(ProxiedPlayer proxiedPlayer) {
        return proxiedPlayer.getName();
    }

    @Override
    public Class<ProxiedPlayer> getPlayerClass() {
        return ProxiedPlayer.class;
    }
}
