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

package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.*;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.bukkit.util.TaskRunnable;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.util.CC;
import org.imanity.framework.util.Stacktrace;
import org.imanity.framework.util.entry.Entry;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service(name = "scoreboard")
public class ImanityBoardService implements TaskRunnable {

    private List<ImanityBoardAdapter> adapters;
    private Queue<Runnable> runnableQueue;
    private AtomicBoolean activated;

    @PreInitialize
    public void preInit() {
        this.adapters = new ArrayList<>();
        this.runnableQueue = new ConcurrentLinkedQueue<>();
        this.activated = new AtomicBoolean(true);
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { ImanityBoardAdapter.class };
            }

            @Override
            public void onEnable(Object instance) {
                addAdapter((ImanityBoardAdapter) instance);
            }
        });
    }

    @PostInitialize
    public void postInit() {
        TaskUtil.runRepeated(this, this.getUpdateTick());
        Events.subscribe(PlayerQuitEvent.class).listen((subscription, event) -> remove(event.getPlayer()));
    }

    public void addAdapter(ImanityBoardAdapter adapter) {
        this.adapters.add(adapter);
        this.adapters.sort(Collections.reverseOrder(Comparator.comparingInt(ImanityBoardAdapter::priority)));
        this.activate();
    }

    private void activate() {
        if (activated.compareAndSet(false, true)) {
            TaskUtil.runRepeated(this, this.getUpdateTick());
        }
    }

    private int getUpdateTick() {
        int tick = 2;
        for (ImanityBoardAdapter adapter : this.adapters) {
            int adapterTick = adapter.tick();
            if (adapterTick != -1) {
                tick = adapterTick;
                break;
            }
        }

        return tick;
    }

    @Override
    public void run(BukkitTask task) {
        try {
            this.tick();

            this.runQueue();
            if (this.adapters.isEmpty()) {
                task.cancel();
                this.activated.set(false);
            }
        } catch (Exception ex) {
            Stacktrace.print(ex);
        }
    }

    public void runQueue() {
        Runnable runnable;
        while ((runnable = this.runnableQueue.poll()) != null) {
            runnable.run();
        }
    }

    private void tick() {
        for (Player player : Imanity.getPlayers()) {

            if (Imanity.SHUTTING_DOWN) {
                break;
            }

            ImanityBoard board = this.get(player);
            if (board == null) {
                continue;
            }

            Entry<String, List<String>> entry = this.findAdapter(player);
            if (entry == null) {
                board.remove();
                continue;
            }
            String title = CC.translate(entry.getKey());

            board.setTitle(title);

            List<String> newLines = entry.getValue();

            if (newLines == null || newLines.isEmpty()) {
                board.remove();
            } else {

                board.setLines(newLines);

            }
        }
    }

    private Entry<String, List<String>> findAdapter(Player player) {
        Entry<String, List<String>> entry = null;

        for (ImanityBoardAdapter adapter : this.adapters) {
            String title = adapter.getTitle(player);
            List<String> list = adapter.getLines(player);
            if (title != null && !title.isEmpty() &&
                    list != null && !list.isEmpty()) {
                entry = new Entry<>(title, list);
                break;
            }
        }

        return entry;
    }

    public void remove(Player player) {
        ImanityBoard board = this.get(player);

        if (board != null) {
            board.remove();
            Metadata.provideForPlayer(player).remove(ImanityBoard.METADATA_TAG);
        }
    }

    public ImanityBoard get(Player player) {
        return Metadata.provideForPlayer(player).getOrNull(ImanityBoard.METADATA_TAG);
    }

    public ImanityBoard getOrCreateScoreboard(Player player) {
        return Metadata.provideForPlayer(player).getOrPut(ImanityBoard.METADATA_TAG, () -> {
            ImanityBoard board = new ImanityBoard(player);
            for (ImanityBoardAdapter adapter : this.adapters) {
                adapter.onBoardCreate(player, board);
            }
            return board;
        });
    }


}
