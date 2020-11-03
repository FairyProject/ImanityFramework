package org.imanity.framework.bukkit;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.util.TaskUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BukkitRepository<T> {

    private ImanityPlugin plugin;

    private boolean async;
    private Function<Player, T> loadAction, findAction;
    private BiConsumer<Player, T> saveAction;

    public BukkitRepository(ImanityPlugin plugin) {
        this.plugin = plugin;
        this.async = ImanityCommon.CORE_CONFIG.ASYNCHRONOUS_DATA_STORING;
    }

    public BukkitRepository<T> async() {
        this.async = true;
        return this;
    }

    public BukkitRepository<T> load(Function<Player, T> loadAction) {
        this.loadAction = loadAction;
        return this;
    }

    public BukkitRepository<T> find(Function<Player, T> findAction) {
        this.findAction = findAction;
        return this;
    }

    public BukkitRepository<T> save(BiConsumer<Player, T> saveAction) {
        this.saveAction = saveAction;
        return this;
    }

    public void init() {
        Preconditions.checkNotNull(this.loadAction, "The loadAction is not being set!");
        Preconditions.checkNotNull(this.saveAction, "The saveAction is not being set!");

        Events.subscribe(PlayerJoinEvent.class)
                .listen((subscription, event) -> {
                    Player player = event.getPlayer();

                    Runnable runnable = () -> this.loadAction.apply(player);

                    if (this.async) {
                        TaskUtil.runAsync(runnable);
                    } else {
                        runnable.run();
                    }
                })
        .build(plugin);

        Events.subscribe(PlayerQuitEvent.class)
                .listen((subscription, event) -> {
                    Player player = event.getPlayer();

                    T t = findAction != null ? findAction.apply(player) : loadAction.apply(player);
                    Runnable runnable = () -> this.saveAction.accept(player, t);

                    if (this.async) {
                        TaskUtil.runAsync(runnable);
                    } else {
                        runnable.run();
                    }
                })
        .build(plugin);
    }

}
