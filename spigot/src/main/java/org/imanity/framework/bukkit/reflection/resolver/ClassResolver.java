package org.imanity.framework.bukkit.reflection.resolver;

import org.imanity.framework.bukkit.reflection.wrapper.ClassWrapper;

/**
 * Default {@link ClassResolver}
 */
public class ClassResolver extends ResolverAbstract<Class> {

	public ClassWrapper resolveWrapper(String... names) {
		return new ClassWrapper<>(resolveSilent(names));
	}

	public Class resolveSilent(String... names) {
		try {
			return resolve(names);
		} catch (Exception e) {
		}
		return null;
	}

	public Class resolve(String... names) throws ClassNotFoundException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		for (String name : names)
			builder.with(name);
		try {
			return super.resolve(builder.build());
		} catch (ReflectiveOperationException e) {
			throw (ClassNotFoundException) e;
		}
	}

	public Class resolveSubClass(Class<?> mainClass, String... names) throws ClassNotFoundException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		String prefix = mainClass.getName() + "$";
		for (String name : names)
			builder.with(prefix + name);
		try {
			return super.resolve(builder.build());
		} catch (ReflectiveOperationException e) {
			throw (ClassNotFoundException) e;
		}
	}

	@Override
	protected Class resolveObject(ResolverQuery query) throws ReflectiveOperationException {
		return Class.forName(query.getName());
	}

	@Override
	protected ClassNotFoundException notFoundException(String joinedNames) {
		return new ClassNotFoundException("Could not resolve class for " + joinedNames);
	}
}
