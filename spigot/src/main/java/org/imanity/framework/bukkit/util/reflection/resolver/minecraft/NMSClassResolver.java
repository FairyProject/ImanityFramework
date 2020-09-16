package org.imanity.framework.bukkit.util.reflection.resolver.minecraft;

import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.ClassResolver;

/**
 * {@link ClassResolver} for <code>net.minecraft.server.*</code> classes
 */
public class NMSClassResolver extends ClassResolver {

	@Override
	public Class resolve(String... names) throws ClassNotFoundException {
		for (int i = 0; i < names.length; i++) {
			if (!names[i].startsWith("net.minecraft.server")) {
				names[i] = "net.minecraft.server." + MinecraftReflection.getVersion() + names[i];
			}
		}
		return super.resolve(names);
	}

	@Override
	public Class resolveSubClass(Class<?> mainClass, String... names) throws ClassNotFoundException {
		String prefix = mainClass.getName() + "$";

		if (!prefix.startsWith("net.minecraft.server")) {
			prefix = "net.minecraft.server." + MinecraftReflection.getVersion() + prefix;
		}

		for (int i = 0; i < names.length; i++) {
			names[i] = prefix + names[i];
		}
		return super.resolve(names);
	}
}
