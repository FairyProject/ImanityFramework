package org.imanity.framework.util;

import lombok.Getter;

import javax.annotation.Nullable;

/**
 * Interface that converts generic objects into types and back.
 *
 * @credit ProtocolLib
 * @author Kristian
 * @param <T> The specific type.
 */
public interface EquivalentConverter<T> {
    /**
     * Retrieve a copy of the generic type from a specific type.
     * <p>
     * This is usually a native net.minecraft.server type in Minecraft.
     * @param specific - the specific type we need to copy.
     * @return A copy of the specific type.
     */
    Object getGeneric(T specific);

    /**
     * Retrieve a copy of the specific type using an instance of the generic type.
     * <p>
     * This is usually a wrapper type in the Bukkit API or ProtocolLib API.
     * @param generic - the generic type.
     * @return The new specific type.
     */
    T getSpecific(Object generic);

    /**
     * Due to type erasure, we need to explicitly keep a reference to the specific type.
     * @return The specific type.
     */
    Class<T> getSpecificType();

    @Getter
    public static class EnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
        private Class<? extends Enum> genericType;
        private Class<T> specificType;

        public EnumConverter(@Nullable Class<? extends Enum> genericType, @Nullable Class<T> specificType) {
            this.genericType = genericType;
            this.specificType = specificType;
        }

        @Override
        public T getSpecific(@Nullable Object generic) {
            if (generic == null) {
                return this.getDefaultSpecific();
            }
            return Enum.valueOf(specificType, ((Enum) generic).name());
        }

        @Override
        public Object getGeneric(@Nullable T specific) {
            if (specific == null) {
                return this.getDefaultGeneric();
            }
            return Enum.valueOf((Class) genericType, specific.name());
        }

        @Nullable
        public T getDefaultSpecific() {
            return null;
        }

        @Nullable
        public Object getDefaultGeneric() {
            return null;
        }

        void setGenericType(Class<? extends Enum> genericType) {
            this.genericType = genericType;
        }
    }
}