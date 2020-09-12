package org.imanity.framework.bukkit.nametag;

import org.bukkit.entity.Player;

final class NameTagUpdate
{
    private String toRefresh;
    private String refreshFor;

    public NameTagUpdate(final Player toRefresh) {
        this.toRefresh = toRefresh.getName();
    }

    public NameTagUpdate(final Player toRefresh, final Player refreshFor) {
        this.toRefresh = toRefresh.getName();
        this.refreshFor = refreshFor.getName();
    }

    public String getToRefresh() {
        return this.toRefresh;
    }

    public String getRefreshFor() {
        return this.refreshFor;
    }
}
