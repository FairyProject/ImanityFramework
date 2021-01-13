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

package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.util.BukkitUtil;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImanityBoardHandler implements Runnable {

    private final ImanityBoardAdapter adapter;
    private final Queue<Runnable> runnables = new ConcurrentLinkedQueue<>();

    public ImanityBoardHandler(ImanityBoardAdapter adapter) {
        this.adapter = adapter;

        Thread thread = new Thread(this);
        thread.setName("Imanity Scoreboard Thread");
        thread.setDaemon(true);
        thread.start();

        Events.subscribe(PlayerQuitEvent.class).listen((subscription, event) -> remove(subscription.getActivePlayer()));
    }

    public void runQueue() {
        Runnable runnable;
        while ((runnable = this.runnables.poll()) != null) {
            runnable.run();
        }
    }

    @Override
    public void run() {
        while (!Imanity.SHUTTING_DOWN) {
            try {
                this.tick();

                this.runQueue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Thread.sleep(this.adapter.tick() * 50L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void tick() {
        for (Player player : Imanity.PLUGIN.getServer().getOnlinePlayers()) {

            if (Imanity.SHUTTING_DOWN) {
                break;
            }

            ImanityBoard board = this.get(player);
            if (board == null) {
                continue;
            }

            String title = BukkitUtil.color(adapter.getTitle(player));

            board.setTitle(title);

            List<String> newLines = this.adapter.getLines(player);

            if (newLines == null || newLines.isEmpty()) {
                board.remove();
            } else {

                board.setLines(newLines);

            }
        }
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
            this.adapter.onBoardCreate(player, board);
            return board;
        });
    }


}
