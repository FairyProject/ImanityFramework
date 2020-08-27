package org.imanity.framework.bukkit.zigguart.utils;

import lombok.Getter;
import org.imanity.framework.bukkit.zigguart.ImanityTabCommons;

@Getter
public class BufferedTabObject {

    private TabColumn column = TabColumn.LEFT;
    private int ping = 0;
    private int slot = 1;
    private String text = "";
    private SkinTexture skinTexture = ImanityTabCommons.defaultTexture;

    public BufferedTabObject text(String text){
        this.text = text;
        return this;
    }

    public BufferedTabObject skin(SkinTexture skinTexture){
        this.skinTexture = skinTexture;
        return this;
    }

    public BufferedTabObject slot(Integer slot){
        this.slot = slot;
        return resizeColumn();
    }

    public BufferedTabObject ping(Integer ping){
        this.ping = ping;
        return resizeColumn();
    }

    public BufferedTabObject column(TabColumn tabColumn){
        this.column = tabColumn;
        return this;
    }

    private BufferedTabObject resizeColumn() {
        if (this.slot > 20 && this.slot <= 40) {
            this.slot = this.slot - 20;
            this.column = TabColumn.MIDDLE;
        } else if (this.slot > 40 && this.slot <= 60) {
            this.slot = this.slot - 40;
            this.column = TabColumn.RIGHT;
        } else if (this.slot > 60 && this.slot <= 80) {
            this.slot = this.slot - 60;
            this.column = TabColumn.FAR_RIGHT;
        }
        return this;
    }

}
