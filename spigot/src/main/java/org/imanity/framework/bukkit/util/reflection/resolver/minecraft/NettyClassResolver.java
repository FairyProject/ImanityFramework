package org.imanity.framework.bukkit.util.reflection.resolver.minecraft;

import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.ClassResolver;

public class NettyClassResolver extends ClassResolver {

    @Override
    public Class resolve(String... names) throws ClassNotFoundException {
        for (int i = 0; i < names.length; i++) {
            if (!names[i].startsWith(MinecraftReflection.NETTY_PREFIX)) {
                names[i] = MinecraftReflection.NETTY_PREFIX + names[i];
            }
        }
        return super.resolve(names);
    }

    @Override
    public Class resolveSubClass(Class<?> mainClass, String... names) throws ClassNotFoundException {
        String prefix = mainClass.getName() + "$";

        if (!prefix.startsWith(MinecraftReflection.NETTY_PREFIX)) {
            prefix = MinecraftReflection.NETTY_PREFIX + prefix;
        }

        for (int i = 0; i < names.length; i++) {
            names[i] = prefix + names[i];
        }
        return super.resolve(names);
    }

}
