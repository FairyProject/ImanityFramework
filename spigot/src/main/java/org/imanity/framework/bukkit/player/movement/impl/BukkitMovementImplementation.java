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
