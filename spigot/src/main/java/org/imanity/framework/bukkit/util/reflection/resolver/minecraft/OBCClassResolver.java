package org.imanity.framework.bukkit.util.reflection.resolver.minecraft;

import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.ClassResolver;

/**
 * {@link ClassResolver} for <code>org.bukkit.craftbukkit.*</code> classes
 */
public class OBCClassResolver extends ClassResolver {

	@Override
	public Class resolve(String... names) throws ClassNotFoundException {
		for (int i = 0; i < names.length; i++) {
			if (!names[i].startsWith("org.bukkit.craftbukkit")) {
				names[i] = "org.bukkit.craftbukkit." + MinecraftReflection.getVersion() + names[i];
			}
		}
		return super.resolve(names);
	}
}
