package org.imanity.framework.bukkit.reflection.wrapper.impl;

public abstract class SignedPropertyImplementation {

    public static SignedPropertyImplementation getImplementation() {
        try {
            Class.forName("com.mojang.authlib.GameProfile");

            return new v1_8();
        } catch (ClassNotFoundException ignored) {
            return new v1_7();
        }
    }

    public abstract Object create(String name, String value, String signature);

    public abstract String getName(Object handle);

    public abstract String getValue(Object handle);

    public abstract String getSignature(Object handle);

    public static class v1_8 extends SignedPropertyImplementation {

        @Override
        public Object create(String name, String value, String signature) {
            return new com.mojang.authlib.properties.Property(name, value, signature);
        }

        @Override
        public String getName(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getName();
        }

        @Override
        public String getValue(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getValue();
        }

        @Override
        public String getSignature(Object handle) {
            return ((com.mojang.authlib.properties.Property) handle).getSignature();
        }
    }

    public static class v1_7 extends SignedPropertyImplementation {

        @Override
        public Object create(String name, String value, String signature) {
            return new net.minecraft.util.com.mojang.authlib.properties.Property(name, value, signature);
        }

        @Override
        public String getName(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getName();
        }

        @Override
        public String getValue(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getValue();
        }

        @Override
        public String getSignature(Object handle) {
            return ((net.minecraft.util.com.mojang.authlib.properties.Property) handle).getSignature();
        }
    }

}
