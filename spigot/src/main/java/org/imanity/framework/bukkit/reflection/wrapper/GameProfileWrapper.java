package org.imanity.framework.bukkit.reflection.wrapper;

import com.google.common.collect.Multimap;
import org.imanity.framework.bukkit.reflection.wrapper.impl.GameProfileImplementation;

import java.util.UUID;

public class GameProfileWrapper extends WrapperAbstract {

    private static final GameProfileImplementation IMPLEMENTATION = GameProfileImplementation.getImplementation();

    protected Object handle;
    private Multimap<String, SignedPropertyWrapper> propertyMap;

    public GameProfileWrapper(Object handle) {
        this.handle = handle;
    }

    public GameProfileWrapper(UUID uuid, String name) {
        this(IMPLEMENTATION.create(name, uuid));
    }

    public String getName() {
        return IMPLEMENTATION.getName(handle);
    }

    public UUID getUuid() {
        return IMPLEMENTATION.getUuid(handle);
    }

    public void setName(String name) {
        IMPLEMENTATION.setName(handle, name);
    }

    public void setUuid(UUID uuid) {
        IMPLEMENTATION.setUuid(handle, uuid);
    }

    public Multimap<String, SignedPropertyWrapper> getProperties() {
        Multimap<String, SignedPropertyWrapper> result = this.propertyMap;

        if (result == null) {
            this.propertyMap = result = IMPLEMENTATION.getProperties(handle);
        }

        return result;
    }

    @Override
    public boolean exists() {
        return handle != null;
    }
}
