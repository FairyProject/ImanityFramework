package org.imanity.framework.bungee.impl;

import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.task.chain.AsyncQueue;
import org.imanity.framework.task.chain.GameInterface;
import org.imanity.framework.task.chain.TaskChainAsyncQueue;
import org.imanity.framework.task.chain.TaskChainFactory;

import java.util.concurrent.TimeUnit;

public class BungeeTaskChainFactory extends TaskChainFactory {

    public BungeeTaskChainFactory() {
        super(new GameInterface() {

            private final AsyncQueue asyncQueue = new TaskChainAsyncQueue();

            @Override
            public boolean isMainThread() {
                return false;
            }

            @Override
            public boolean hasMainThread() {
                return false;
            }

            @Override
            public AsyncQueue getAsyncQueue() {
                return asyncQueue;
            }

            @Override
            public void postToMain(Runnable run) {
                Imanity.getProxy().getScheduler().runAsync(Imanity.PLUGIN, run);
            }

            @Override
            public void scheduleTask(int gameUnits, Runnable run) {
                Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, run, gameUnits * 50L, TimeUnit.MILLISECONDS);
            }

            @Override
            public void registerShutdownHandler(TaskChainFactory factory) {

            }
        });
    }
}
