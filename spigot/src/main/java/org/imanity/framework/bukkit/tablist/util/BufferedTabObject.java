package org.imanity.framework.bukkit.tablist.util;

import lombok.Getter;

@Getter
public class BufferedTabObject {
    private TabColumn column;
    private Integer ping;
    private int slot;
    private String text;
    private TabIcon tabIcon;

    public BufferedTabObject() {
        this.column = TabColumn.LEFT;
        this.slot = 1;
        this.text = "";
        this.tabIcon = TabIcon.GRAY;
    }

    public BufferedTabObject text(String text) {
        this.text = text;
        return this;
    }

    public BufferedTabObject skin(TabIcon tabIcon) {
        this.tabIcon = tabIcon;
        return this;
    }

    public BufferedTabObject slot(Integer slot) {
        this.slot = slot;
        return this;
    }

    public BufferedTabObject ping(Integer ping) {
        this.ping = ping;
        return this;
    }

    public BufferedTabObject column(TabColumn tabColumn) {
        this.column = tabColumn;
        return this;
    }
}
