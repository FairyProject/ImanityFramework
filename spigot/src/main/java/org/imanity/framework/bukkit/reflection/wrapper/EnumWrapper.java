package org.imanity.framework.bukkit.reflection.wrapper;

public class EnumWrapper extends WrapperAbstract {

    private final Class<? extends Enum> classType;

    public EnumWrapper(Class<?> classType) {
        this.classType = classType.asSubclass(Enum.class);
    }

    public Enum getType(String enumName) {

        try {

            return Enum.valueOf(this.classType, enumName);

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    public boolean exists() {
        return this.classType != null;
    }
}
