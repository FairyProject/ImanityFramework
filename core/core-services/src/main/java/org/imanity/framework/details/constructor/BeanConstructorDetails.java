package org.imanity.framework.details.constructor;

import org.imanity.framework.BeanContext;

public interface BeanConstructorDetails {
    Object newInstance(BeanContext beanContext);

    Class<?> getType();

    java.lang.reflect.Constructor<?> getConstructor();

    Class<?>[] getParameterTypes();
}
