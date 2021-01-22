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

import org.imanity.framework.config.classes.TestClass;
import org.imanity.framework.config.Reflect;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.imanity.framework.config.util.CollectionFactory.setOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReflectTest {
    private static final Set<Class<?>> ALL_SIMPLE_TYPES = setOf(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            String.class
    );

    @Test
    void isSimpleType() {
        for (Class<?> cls : ALL_SIMPLE_TYPES) {
            assertThat(Reflect.isSimpleType(cls), is(true));
        }
        assertThat(Reflect.isSimpleType(Object.class), is(false));
    }

    @Test
    void isContainerType() {
        assertThat(Reflect.isContainerType(Object.class), is(false));
        assertThat(Reflect.isContainerType(HashMap.class), is(true));
        assertThat(Reflect.isContainerType(HashSet.class), is(true));
        assertThat(Reflect.isContainerType(ArrayList.class), is(true));
    }

    @Test
    void getValue() throws NoSuchFieldException {
        TestClass testClass = TestClass.TEST_VALUES;
        Field f1 = TestClass.class.getDeclaredField("string");
        Field f2 = TestClass.class.getDeclaredField("primLong");
        Field f3 = TestClass.class.getDeclaredField("staticFinalInt");

        Object value = Reflect.getValue(f1, testClass);
        assertThat(value, is(testClass.getString()));

        value = Reflect.getValue(f2, testClass);
        assertThat(value, is(testClass.getPrimLong()));

        value = Reflect.getValue(f3, testClass);
        assertThat(value, is(TestClass.getStaticFinalInt()));
    }
}