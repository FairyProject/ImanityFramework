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

package org.imanity.framework.config.classes;

import org.imanity.framework.config.annotation.ConfigurationElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.imanity.framework.config.util.CollectionFactory.*;

@ConfigurationElement
public final class TestSubSubClass {
    public static final TestSubSubClass TEST_VALUES;

    static {
        TEST_VALUES = new TestSubSubClass();
        TEST_VALUES.primInt = 1;
        TEST_VALUES.string = "string";
        TEST_VALUES.list = listOf("list");
        TEST_VALUES.set = setOf("set");
        TEST_VALUES.map = mapOf("map", 1);
    }

    private int primInt;
    private String string = "";
    private List<String> list = listOf();
    private Set<String> set = setOf();
    private Map<String, Integer> map = mapOf();

    public static TestSubSubClass of(int primInt, String string) {
        TestSubSubClass cls = new TestSubSubClass();
        cls.primInt = primInt;
        cls.string = string;
        String concat = string + string;
        cls.list = listOf(concat);
        cls.set = setOf(concat);
        cls.map = mapOf(concat, primInt);
        return cls;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> asMap = mapOf("primInt", primInt, "string", string);
        asMap.put("list", list);
        asMap.put("set", set);
        asMap.put("map", map);
        return asMap;
    }

    @Override
    public String toString() {
        return "TestSubSubClass{" +
                "primInt=" + primInt +
                ", string='" + string + '\'' +
                ", list=" + list +
                ", set=" + set +
                ", map=" + map +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestSubSubClass that = (TestSubSubClass) o;

        if (primInt != that.primInt) return false;
        if (!string.equals(that.string)) return false;
        if (!list.equals(that.list)) return false;
        if (!set.equals(that.set)) return false;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        int result = primInt;
        result = 31 * result + string.hashCode();
        result = 31 * result + list.hashCode();
        result = 31 * result + set.hashCode();
        result = 31 * result + map.hashCode();
        return result;
    }
}
