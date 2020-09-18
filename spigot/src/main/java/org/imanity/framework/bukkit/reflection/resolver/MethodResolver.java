package org.imanity.framework.bukkit.reflection.resolver;

import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.CommonUtility;

import java.lang.reflect.Method;

/**
 * Resolver for methods
 *
 * @credit ProtocolLib
 */
public class MethodResolver extends MemberResolver<Method> {

	public MethodResolver(Class<?> clazz) {
		super(clazz);
	}

	public MethodResolver(String className) throws ClassNotFoundException {
		super(className);
	}

	public Method resolveSignature(String... signatures)throws ReflectiveOperationException {
		for (Method method : clazz.getDeclaredMethods()) {
			String methodSignature = MethodWrapper.getMethodSignature(method);
			for (String s : signatures) {
				if (s.equals(methodSignature)) {
					return AccessUtil.setAccessible(method);
				}
			}
		}
		return null;
	}

	public Method resolveSignatureSilent(String... signatures) {
		try {
			return resolveSignature(signatures);
		} catch (ReflectiveOperationException ignored) {
		}
		return null;
	}

	public MethodWrapper resolveSignatureWrapper(String... signatures) {
		return new MethodWrapper(resolveSignatureSilent(signatures));
	}

	public MethodWrapper resolve(int index, Class<?>... parameters) throws ReflectiveOperationException {

		return this.resolve(null, index, parameters);

	}

	public MethodWrapper resolve(Class<?> returnType, int index, Class<?>... parameters) throws ReflectiveOperationException {

		int currentIndex = 0;
		for (Method method : this.clazz.getDeclaredMethods()) {
			if ((returnType == null || method.getReturnType() == returnType) &&
					(parameters.length == 0 || isParametersEquals(parameters, method.getParameterTypes())) &&
					index == currentIndex++) {
				return new MethodWrapper(AccessUtil.setAccessible(method));
			}
		}

		throw new NoSuchMethodException();

	}

	@Override
	public Method resolveIndex(int index) throws IndexOutOfBoundsException, ReflectiveOperationException {
		return AccessUtil.setAccessible(this.clazz.getDeclaredMethods()[index]);
	}

	@Override
	public Method resolveIndexSilent(int index) {
		try {
			return resolveIndex(index);
		} catch (IndexOutOfBoundsException | ReflectiveOperationException ignored) {
		}
		return null;
	}

	@Override
	public MethodWrapper resolveIndexWrapper(int index) {
		return new MethodWrapper<>(resolveIndexSilent(index));
	}

	public MethodWrapper resolveWrapper(String... names) {
		return new MethodWrapper<>(resolveSilent(names));
	}

	public MethodWrapper resolveWrapper(ResolverQuery... queries) {
		return new MethodWrapper<>(resolveSilent(queries));
	}

	public Method resolveSilent(String... names) {
		try {
			return resolve(names);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public Method resolveSilent(ResolverQuery... queries) {
		return super.resolveSilent(queries);
	}

	public Method resolve(String... names) throws NoSuchMethodException {
		ResolverQuery.Builder builder = ResolverQuery.builder();
		for (String name : names) {
			builder.with(name);
		}
		return resolve(builder.build());
	}

	@Override
	public Method resolve(ResolverQuery... queries) throws NoSuchMethodException {
		try {
			return super.resolve(queries);
		} catch (ReflectiveOperationException e) {
			throw (NoSuchMethodException) e;
		}
	}

	@Override
	protected Method resolveObject(ResolverQuery query) throws ReflectiveOperationException {
		for (Method method : this.clazz.getDeclaredMethods()) {
			if (method.getName().equals(query.getName()) && (query.getTypes().length == 0 || isParametersEquals(query.getTypes(), method.getParameterTypes()))) {
				return AccessUtil.setAccessible(method);
			}
		}
		throw new NoSuchMethodException();
	}

	@Override
	protected NoSuchMethodException notFoundException(String joinedNames) {
		return new NoSuchMethodException("Could not resolve method for " + joinedNames + " in class " + this.clazz);
	}

	static boolean isParametersEquals(Class<?>[] l1, Class<?>[] l2) {
		boolean equal = true;
		if (l1.length != l2.length) { return false; }
		for (int i = 0; i < l1.length; i++) {
			if (CommonUtility.wrapPrimitiveToObject(l1[i]) != CommonUtility.wrapPrimitiveToObject(l2[i])) {
				equal = false;
				break;
			}
		}
		return equal;
	}
}
