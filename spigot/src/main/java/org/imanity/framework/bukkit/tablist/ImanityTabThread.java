package org.imanity.framework.bukkit.tablist;

public class ImanityTabThread extends Thread {

    private ImanityTabHandler imanityTabHandler;

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
                sleep(imanityTabHandler.getTicks() * 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        for (ImanityTablist tablist : imanityTabHandler.getTablists().values()) {
            tablist.update();
        }
    }
}
