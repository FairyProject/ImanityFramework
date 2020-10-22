/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.yaml;

import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.imanity.framework.config.yaml.YamlConfiguration;

import java.nio.file.Path;

/**
 * A {@code BukkitYamlConfiguration} is a specialized form of a
 * {@code YamlConfiguration} that uses better default values.
 */
public abstract class BukkitYamlConfiguration extends YamlConfiguration {
    protected BukkitYamlConfiguration(Path path, BukkitYamlProperties properties) {
        super(path, properties);
    }

    protected BukkitYamlConfiguration(Path path) {
        this(path, BukkitYamlProperties.DEFAULT);
    }

    public static class BukkitYamlProperties extends YamlProperties {
        public static final BukkitYamlProperties DEFAULT = builder().build();

        private BukkitYamlProperties(Builder<?> builder) {
            super(builder);
        }

        public static Builder<?> builder() {
            return new Builder() {
                @Override
                protected Builder<?> getThis() {
                    return this;
                }
            };
        }

        public static abstract class
        Builder<B extends BukkitYamlProperties.Builder<B>>
                extends YamlProperties.Builder<B> {

            protected Builder() {
                setConstructor(new YamlConstructor());
                setRepresenter(new YamlRepresenter());
            }

            /**
             * Builds a new {@code BukkitYamlProperties} instance using the values set.
             *
             * @return new {@code BukkitYamlProperties} instance
             */
            public BukkitYamlProperties build() {
                return new BukkitYamlProperties(this);
            }
        }
    }
}
