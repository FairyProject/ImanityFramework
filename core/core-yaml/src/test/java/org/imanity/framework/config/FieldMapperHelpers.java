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

package org.imanity.framework.config;

import org.imanity.framework.config.FieldMapper.MappingInfo;
import org.imanity.framework.config.annotation.ConfigurationElement;

import java.util.Map;

import static org.imanity.framework.config.yaml.YamlConfiguration.YamlProperties.DEFAULT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldMapperHelpers {
    @ConfigurationElement
    interface LocalTestInterface {}

    @ConfigurationElement
    static class LocalTestInterfaceImpl implements LocalTestInterface {}

    @ConfigurationElement
    static abstract class LocalTestAbstractClass {}

    @ConfigurationElement
    static class LocalTestAbstractClassImpl extends LocalTestAbstractClass {}

    @ConfigurationElement
    enum LocalTestEnum {
        S, T
    }

    static class Sub1 {}

    @ConfigurationElement
    static class Sub2 {}

    @ConfigurationElement
    static class Sub3 {
        public Sub3(int x) {}
    }

    public static void assertItmCfgExceptionMessage(Object o, String msg) {
        ConfigurationException ex = assertItmThrowsCfgException(o);
        assertThat(ex.getMessage(), is(msg));
    }

    public static void assertIfmCfgExceptionMessage(
            Object o, Map<String, Object> map, String msg
    ) {
        ConfigurationException ex = assertIfmThrowsCfgException(o, map);
        assertThat(ex.getMessage(), is(msg));
    }

    public static ConfigurationException assertItmThrowsCfgException(Object o) {
        return assertThrows(
                ConfigurationException.class,
                () -> instanceToMap(o)
        );
    }

    public static ConfigurationException assertIfmThrowsCfgException(
            Object o, Map<String, Object> map
    ) {
        return assertThrows(
                ConfigurationException.class,
                () -> instanceFromMap(o, map)
        );
    }

    public static Map<String, Object> instanceToMap(Object o) {
        return instanceToMap(o, DEFAULT);
    }

    public static Map<String, Object> instanceToMap(
            Object o, Configuration.Properties props
    ) {
        MappingInfo mappingInfo = new MappingInfo(null, props);
        return FieldMapper.instanceToMap(o, mappingInfo);
    }

    public static <T> T instanceFromMap(T o, Map<String, Object> map) {
        return instanceFromMap(o, map, DEFAULT);
    }

    public static <T> T instanceFromMap(
            T o, Map<String, Object> map, Configuration.Properties props
    ) {
        MappingInfo mappingInfo = new MappingInfo(null, props);
        FieldMapper.instanceFromMap(o, map, mappingInfo);
        return o;
    }
}
