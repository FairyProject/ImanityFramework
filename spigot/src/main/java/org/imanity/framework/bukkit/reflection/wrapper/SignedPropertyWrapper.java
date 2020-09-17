package org.imanity.framework.bukkit.reflection.wrapper;

import lombok.Getter;
import org.imanity.framework.bukkit.reflection.wrapper.impl.SignedPropertyImplementation;

public class SignedPropertyWrapper extends WrapperAbstract {

    private static final SignedPropertyImplementation IMPLEMENTATION = SignedPropertyImplementation.getImplementation();

    @Getter
    private Object handle;

    public SignedPropertyWrapper(Object handle) {
        this.handle = handle;
    }

    public SignedPropertyWrapper(String name, String value, String signature) {
        this(IMPLEMENTATION.create(name, value, signature));
    }

    @Override
    public boolean exists() {
        return handle != null;
    }

    public String getName() {
        return IMPLEMENTATION.getName(handle);
    }

    public String getValue() {
        return IMPLEMENTATION.getValue(handle);
    }

    public String getSignature() {
        return IMPLEMENTATION.getSignature(handle);
    }

    public SignedPropertyWrapper withName(String name) {
        this.handle = IMPLEMENTATION.create(name, this.getValue(), this.getSignature());
        return this;
    }

    public SignedPropertyWrapper withValue(String value) {
        this.handle = IMPLEMENTATION.create(this.getName(), value, this.getSignature());
        return this;
    }

    public SignedPropertyWrapper withSignature(String signature) {
        this.handle = IMPLEMENTATION.create(this.getName(), this.getValue(), signature);
        return this;
    }
}
