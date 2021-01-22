/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.bukkit;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.util.TaskUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BukkitRepository<T> {

    private final Plugin plugin;

    private boolean async;
    private Function<Player, T> loadAction, findAction;
    private BiConsumer<Player, T> saveAction, onLoaded, onSaved;

    public BukkitRepository(Plugin plugin) {
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

    public BukkitRepository<T> onLoaded(BiConsumer<Player, T> consumer) {
        this.onLoaded = consumer;
        return this;
    }

    public BukkitRepository<T> onSaved(BiConsumer<Player, T> consumer) {
        this.onSaved = consumer;
        return this;
    }

    public void init() {
        Preconditions.checkNotNull(this.loadAction, "The loadAction is not being set!");

        Events.subscribe(PlayerJoinEvent.class)
                .listen((subscription, event) -> {
                    Player player = event.getPlayer();

                    Runnable runnable = () -> {
                        T t = this.loadAction.apply(player);
                        if (this.onLoaded != null) {
                            this.onLoaded.accept(player, t);
                        }
                    };

                    if (this.async) {
                        TaskUtil.runAsync(runnable);
                    } else {
                        runnable.run();
                    }
                })
        .build(plugin);

        if (this.saveAction != null) {
            Events.subscribe(PlayerQuitEvent.class)
                    .listen((subscription, event) -> {
                        Player player = event.getPlayer();

                        Runnable runnable = () -> {
                            T t = findAction != null ? findAction.apply(player) : loadAction.apply(player);
                            this.saveAction.accept(player, t);
                            if (this.onSaved != null) {
                                this.onSaved.accept(player, t);
                            }
                        };

                        if (this.async) {
                            TaskUtil.runAsync(runnable);
                        } else {
                            runnable.run();
                        }
                    })
                    .build(plugin);
        }
    }

}
