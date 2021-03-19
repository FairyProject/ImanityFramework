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

package org.imanity.framework.details;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.imanity.framework.Service;
import org.imanity.framework.details.GenericBeanDetails;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class DependenciesBeanDetails extends GenericBeanDetails {

    @Getter
    protected final Set<String> dependencies;

    public DependenciesBeanDetails(Object instance) {
        this(instance.getClass(), instance, "dummy");
    }

    public DependenciesBeanDetails(Object instance, Service service) {
        this(instance.getClass(), instance, service.name());
    }

    public DependenciesBeanDetails(Class<?> type, String name) {
        super(type, name);
        this.dependencies = new HashSet<>();
    }

    public DependenciesBeanDetails(Class<?> type, String name, String[] dependencies) {
        super(type, name);
        this.dependencies = Sets.newHashSet(dependencies);
    }

    public DependenciesBeanDetails(Class<?> type, @Nullable Object instance, String name) {
        super(type, instance, name);
        this.dependencies = new HashSet<>();
    }

    public DependenciesBeanDetails(Class<?> type, @Nullable Object instance, String name, String[] dependencies) {
        super(type, instance, name);
        this.dependencies = Sets.newHashSet(dependencies);
    }

    @Override
    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }
}
