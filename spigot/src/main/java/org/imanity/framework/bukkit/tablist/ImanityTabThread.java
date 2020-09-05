package org.imanity.framework.bukkit.tablist;

public class ImanityTabThread extends Thread {

    private final ImanityTabHandler imanityTabHandler;

    public ImanityTabThread(ImanityTabHandler imanityTabHandler) {
        this.imanityTabHandler = imanityTabHandler;
        this.start();
    }

    @Override
    public void run() {
        while(true) {
            //Tick
            try {
                tick();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //Thread Sleep
            try {
                Thread.sleep(imanityTabHandler.getTicks() * 50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void tick() {
        for (ImanityTablist tablist : imanityTabHandler.getTablists().values()) {
            tablist.update();
        }
    }
}
