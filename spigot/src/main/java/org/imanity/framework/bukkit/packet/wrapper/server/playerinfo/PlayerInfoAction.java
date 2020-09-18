package org.imanity.framework.bukkit.packet.wrapper.server.playerinfo;

import lombok.Getter;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.util.EquivalentConverter;

public enum PlayerInfoAction {

    ADD_PLAYER(0),
    UPDATE_GAME_MODE(1),
    UPDATE_LATENCY(2),
    UPDATE_DISPLAY_NAME(3),
    REMOVE_PLAYER(4);

    @Getter
    private final int id;

    PlayerInfoAction(int id) {
        this.id = id;
    }

    public static PlayerInfoAction getById(int id) {
        for (PlayerInfoAction action : PlayerInfoAction.values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null;
    }

    private static EquivalentConverter.EnumConverter<PlayerInfoAction> CONVERTER;

    public static EquivalentConverter.EnumConverter<PlayerInfoAction> getConverter() {
        if (CONVERTER != null) {
            return CONVERTER;
        }

        return CONVERTER = new EquivalentConverter.EnumConverter<>(getGenericType(), PlayerInfoAction.class);
    }

    public static Class<? extends Enum> getGenericType() {
        NMSClassResolver classResolver = new NMSClassResolver();
        Class<?> type = classResolver.resolveSilent("PlayerInfoAction");

        if (type == null) {
            type = classResolver.resolveSilent("EnumPlayerInfoAction");

            if (type == null) {
                try {
                    Class<?> mainClass = classResolver.resolve("PacketPlayOutPlayerInfo");
                    type = classResolver.resolveSubClass(mainClass, "EnumPlayerInfoAction");
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            classResolver.cache("PlayerInfoAction", type);
        }

        return (Class<? extends Enum>) type;
    }

}