/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
