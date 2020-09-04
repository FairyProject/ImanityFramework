package org.imanity.framework.bukkit.tablist.utils;

import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.OfflinePlayer;
import org.imanity.framework.bukkit.tablist.ImanityTablist;

import java.util.UUID;

@Getter @Setter @AllArgsConstructor
public class TabEntry {

    private String id;
    private UUID uuid;
    private String text;
    private ImanityTablist tab;
    private SkinTexture texture;
    private TabColumn column;
    private int slot;
    private int rawSlot;
    private int latency;

}
