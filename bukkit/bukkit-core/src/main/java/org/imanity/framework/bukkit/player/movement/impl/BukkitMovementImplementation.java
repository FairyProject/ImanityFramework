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

package org.imanity.framework.bukkit.player.movement.impl;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.EventSubscribeBuilder;
import org.imanity.framework.bukkit.listener.events.EventSubscription;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.player.movement.MovementListener;

public class BukkitMovementImplementation extends AbstractMovementImplementation {

    private EventSubscription<PlayerMoveEvent> subscription;

    public BukkitMovementImplementation(MovementListener movementListener) {
        super(movementListener);
    }

    @Override
    public void register() {
        EventSubscribeBuilder<PlayerMoveEvent> subscribeBuilder = Events.subscribe(PlayerMoveEvent.class)
                .handleSubClasses()
                .listen((handler, event) -> {

                    Player player = event.getPlayer();
                    Location from = event.getFrom();
                    Location to = event.getTo();

                    this.updateLocation(player, from, to);
                    this.updateRotation(player, from, to);

                });

        if (this.isIgnoreSameBlock()) {
            if (this.isIgnoreSameY()) {
                subscribeBuilder.filter(Events.IGNORE_SAME_BLOCK_AND_Y);
            } else {
                subscribeBuilder.filter(Events.IGNORE_SAME_BLOCK);
            }
        }

        this.subscription = subscribeBuilder.build(Imanity.PLUGIN);
    }

    @Override
    public void unregister() {
        this.subscription.unregister();
    }
}
