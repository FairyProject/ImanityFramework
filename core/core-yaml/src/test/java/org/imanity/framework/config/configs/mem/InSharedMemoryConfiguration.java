/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.config.configs.mem;

import org.imanity.framework.config.Configuration;
import org.imanity.framework.config.ConfigurationSource;

import java.util.Map;
import java.util.Objects;

public class InSharedMemoryConfiguration
        extends Configuration<InSharedMemoryConfiguration> {
    private final InSharedMemorySource source = new InSharedMemorySource();

    protected InSharedMemoryConfiguration(Properties properties) {
        super(properties);
    }

    @Override
    protected ConfigurationSource<InSharedMemoryConfiguration> getSource() {
        return source;
    }

    @Override
    protected InSharedMemoryConfiguration getThis() {
        return this;
    }

    private static final class InSharedMemorySource implements
            ConfigurationSource<InSharedMemoryConfiguration> {
        private static Map<String, Object> map;

        @Override
        public Map<String, Object> loadConfiguration(
                InSharedMemoryConfiguration config
        ) {
            return Objects.requireNonNull(map);
        }

        @Override
        public void saveConfiguration(
                InSharedMemoryConfiguration config, Map<String, Object> map
        ) {
            InSharedMemorySource.map = map;
        }
    }
}
