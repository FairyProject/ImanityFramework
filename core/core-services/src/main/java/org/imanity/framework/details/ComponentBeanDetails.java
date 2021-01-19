package org.imanity.framework.details;

import org.imanity.framework.Component;
import org.imanity.framework.ComponentHolder;
import org.imanity.framework.Service;
import org.jetbrains.annotations.Nullable;

public class ComponentBeanDetails extends GenericBeanDetails {

    private ComponentHolder componentHolder;

    public ComponentBeanDetails(Class<?> type, @Nullable Object instance, String name, ComponentHolder componentHolder) {
        super(type, instance, name);

        this.componentHolder = componentHolder;
    }

    @Override
    public void onEnable() {
        this.componentHolder.onEnable(this.getInstance());
    }

    @Override
    public void onDisable() {
        this.componentHolder.onDisable(this.getInstance());
    }
}
