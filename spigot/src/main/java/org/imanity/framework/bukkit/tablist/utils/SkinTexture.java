package org.imanity.framework.bukkit.tablist.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.tablist.ImanityTabCommons;

@Setter
public class SkinTexture {

    public String SKIN_VALUE;
    public String SKIN_SIGNATURE;

    public SkinTexture(String skinValue, String skinSig) {
        this.SKIN_VALUE = skinValue;
        this.SKIN_SIGNATURE = skinSig;
    }

    @Override
    public String toString() {
        return SKIN_SIGNATURE + SKIN_VALUE;
    }

    public static SkinTexture fromPlayer(Player player) {
        EntityPlayer playerNMS = ((CraftPlayer) player).getHandle();
        GameProfile profile = playerNMS.getProfile();

        if (!profile.getProperties().get("textures").isEmpty()) {
            Property property = profile.getProperties().get("textures").iterator().next();
            String texture = property.getValue();
            String signature = property.getSignature();

            return new SkinTexture(texture, signature);
        }

        return ImanityTabCommons.defaultTexture;
    }
}
