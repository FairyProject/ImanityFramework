package org.imanity.framework.details.constructor;

import lombok.Getter;
import lombok.SneakyThrows;
import org.imanity.framework.Autowired;
import org.imanity.framework.BeanConstructor;
import org.imanity.framework.BeanContext;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.util.AccessUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Getter
public class GenericBeanConstructorDetails implements BeanConstructorDetails {

    private final Class<?> type;
    private final Constructor<?> constructor;
    private final Class<?>[] parameterTypes;

    @SneakyThrows
    public GenericBeanConstructorDetails(Class<?> type, BeanContext beanContext) {
        this.type = type;

        Constructor<?> constructorRet = null;
        int priorityRet = -1;

        for (Constructor<?> constructor : this.type.getConstructors()) {
            AccessUtil.setAccessible(constructor);

            int priority = -1;
            BeanConstructor annotation = constructor.getAnnotation(BeanConstructor.class);
            if (annotation != null) {
                priority = annotation.priority();
            }

            if (constructorRet == null || priorityRet < priority) {
                constructorRet = constructor;
                priorityRet = priority;
            }
        }

        this.constructor = constructorRet;
        this.parameterTypes = this.constructor.getParameterTypes();
        for (Class<?> parameter : this.parameterTypes) {
            if (!beanContext.isBean(parameter)) {
                throw new IllegalArgumentException("The type " + parameter.getName() + ", it's not supposed to be in bean constructor!");
            }
        }
    }

    @Override
    public Object newInstance(BeanContext beanContext) {
        Object[] parameters = new Object[this.parameterTypes.length];

        for (int i = 0; i < parameters.length; i++) {
            Object bean = beanContext.getBean(this.parameterTypes[i]);
            if (bean == null) {
                throw new IllegalArgumentException("Couldn't find bean " + this.parameterTypes[i].getName() + "!");
            }

            parameters[i] = bean;
        }

        try {
            return this.constructor.newInstance(parameters);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Something wrong while constructing " + this.type.getName() + "!", e);
        }
    }

}
