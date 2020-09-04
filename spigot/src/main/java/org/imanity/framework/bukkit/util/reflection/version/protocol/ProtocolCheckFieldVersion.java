package org.imanity.framework.bukkit.util.reflection.version.protocol;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.ResolverQuery;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.FieldWrapper;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.MethodWrapper;

public class ProtocolCheckFieldVersion implements ProtocolCheck {

    private final FieldWrapper PLAYER_CONNECTION_FIELD;
    private final FieldWrapper NETWORK_MANAGER_FIELD;
    private final FieldWrapper VERSION_FIELD;

    public ProtocolCheckFieldVersion() {
        NMSClassResolver nmsClassResolver = new NMSClassResolver();
        try {
            Class<?> networkManager = nmsClassResolver.resolve("NetworkManager");
            Class<?> playerConnection = nmsClassResolver.resolve("PlayerConnection");
            Class<?> entityPlayer = nmsClassResolver.resolve("EntityPlayer");

            FieldResolver fieldResolver = new FieldResolver(entityPlayer);

            PLAYER_CONNECTION_FIELD = fieldResolver.resolveByFirstTypeWrapper(playerConnection);

            fieldResolver = new FieldResolver(playerConnection);

            NETWORK_MANAGER_FIELD = fieldResolver.resolveByFirstTypeWrapper(networkManager);

            fieldResolver = new FieldResolver(networkManager);
            VERSION_FIELD = fieldResolver.resolveWrapper("version");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVersion(Player player) {
        Object entityPlayer = MinecraftReflection.getHandleSilent(player);
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(entityPlayer);
        Object networkManager = NETWORK_MANAGER_FIELD.get(playerConnection);

        return (int) VERSION_FIELD.get(networkManager);
    }
}
