package org.imanity.framework.details;

import lombok.SneakyThrows;
import org.imanity.framework.plugin.AbstractPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public interface BeanDetails {

    boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException;

    void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException;

    boolean isStage(ActivationStage stage);

    boolean isActivated();

    boolean isDestroyed();

    @Nullable
    String getTag(String key);

    boolean hasTag(String key);

    void addTag(String key, String value);

    void setName(String name);

    void setStage(ActivationStage stage);

    void setDisallowAnnotations(java.util.Map<Class<? extends Annotation>, String> disallowAnnotations);

    void setAnnotatedMethods(java.util.Map<Class<? extends Annotation>, java.util.Collection<Method>> annotatedMethods);

    void setInstance(Object instance);

    void setType(Class<?> type);

    void setTags(java.util.Map<String, String> tags);

    String getName();

    ActivationStage getStage();

    Map<Class<? extends Annotation>, String> getDisallowAnnotations();

    Map<Class<? extends Annotation>, java.util.Collection<Method>> getAnnotatedMethods();

    @Nullable
    Object getInstance();

    Class<?> getType();

    Map<String, String> getTags();

    void bindWith(AbstractPlugin plugin);

    AbstractPlugin getBindPlugin();

    boolean isBind();

    default void onEnable() {

    }

    default void onDisable() {

    }

    enum ActivationStage {

        NOT_LOADED,
        PRE_INIT_CALLED,
        POST_INIT_CALLED,

        PRE_DESTROY_CALLED,
        POST_DESTROY_CALLED

    }

}
