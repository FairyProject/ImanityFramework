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

package org.imanity.framework.config.filter;

import org.imanity.framework.config.classes.ClassWithFinalStaticTransientField;
import org.imanity.framework.config.filter.FieldFilter;
import org.imanity.framework.config.filter.FieldFilters;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FieldFilterTest {
    private static final FieldFilter filter = FieldFilters.DEFAULT;
    private static final Class<ClassWithFinalStaticTransientField> CWFSTF =
            ClassWithFinalStaticTransientField.class;

    @Test
    void filteredFieldsFiltersFields() throws NoSuchFieldException {
        List<? extends Field> fields = filter.filterDeclaredFieldsOf(CWFSTF);
        assertThat(fields.size(), is(0));

        class A {
            private int i;
            private final int j = 0;
            private transient int k;
        }
        fields = filter.filterDeclaredFieldsOf(A.class);
        assertThat(fields.size(), is(1));
        assertThat(fields.get(0), is(A.class.getDeclaredField("i")));
    }

    @Test
    void defaultFilterFiltersSyntheticFields() {
        for (Field field : ClassWithSyntheticField.class.getDeclaredFields()) {
            assertThat(field.isSynthetic(), is(true));
            assertThat(filter.test(field), is(false));
        }
    }

    @Test
    void defaultFilterFiltersFinalStaticTransientFields()
            throws NoSuchFieldException {
        Field field = CWFSTF.getDeclaredField("i");
        assertThat(Modifier.isFinal(field.getModifiers()), is(true));
        assertThat(filter.test(field), is(false));

        field = CWFSTF.getDeclaredField("j");
        assertThat(Modifier.isStatic(field.getModifiers()), is(true));
        assertThat(filter.test(field), is(false));

        field = CWFSTF.getDeclaredField("k");
        assertThat(Modifier.isTransient(field.getModifiers()), is(true));
        assertThat(filter.test(field), is(false));
    }

    private final class ClassWithSyntheticField {}
}