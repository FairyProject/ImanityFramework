package org.imanity.framework.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;

import java.lang.reflect.Field;

public class EntityPacketUtil {

    //Byte = 1.7.10->1.8.8, Int = 1.9->1.15.x, Short = 1.16.x
    @Getter private static byte mode = 0; //byte = 0, int = 1, short = 2
    @Getter private static double dXYZDivisor = 0.0;

    public static void init() {
        Class<?> packetClass = PacketTypeClasses.Server.ENTITY;

        try {
            FieldResolver fieldResolver = new FieldResolver(packetClass);
            Field dxField = fieldResolver.resolveIndex(1);
            assert dxField != null;
            if (dxField.equals(fieldResolver.resolve(byte.class, 0).getField())) {
                mode = 0;
            } else if (dxField.equals(fieldResolver.resolve(int.class, 1).getField())) {
                mode = 1;
            } else if (dxField.equals(fieldResolver.resolve(short.class, 0).getField())) {
                mode = 2;
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        if (mode == 0) {
            dXYZDivisor = 32.0;
        } else {
            dXYZDivisor = 4096.0;
        }

    }

}
