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

package org.imanity.framework.bukkit.impl;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.ComponentHolder;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.FilteredListener;
import org.imanity.framework.bukkit.reflection.resolver.ConstructorResolver;

public class ComponentHolderBukkitListener extends ComponentHolder {

    @Override
    public Object newInstance(Class<?> type) {
        Plugin plugin;
        if (Imanity.TESTING) {
            plugin = Imanity.PLUGIN;
        } else {
            plugin = JavaPlugin.getProvidingPlugin(type);
        }

        Object object;

        try {
            ConstructorResolver resolver = new ConstructorResolver(type);
            object = resolver.resolveMatches(
                    new Class[0],
                    new Class[] {plugin.getClass()},
                    new Class[] {JavaPlugin.class}
            )
                    .resolve(
                            new Object[0],
                            new Object[] { plugin }
                    );
        } catch (Throwable throwable) {
            Imanity.LOGGER.error("Something wrong while new instance of listener " + type.getName() + "!", throwable);
            return null;
        }

        if (object == null) {
            Imanity.LOGGER.error("The Listener " + type.getName() + " Doesn't have matches arguments! (It would either be no args or 1 arg with plugin it self)");
            return null;
        }

        if (Listener.class.isAssignableFrom(type)) {

            Imanity.registerEvents((Listener) object);

        } else if (!FilteredListener.class.isAssignableFrom(type)) {
            Imanity.LOGGER.error("The Class " + type.getSimpleName() + " wasn't implement Listener or FunctionListener!");

            return null;
        }

        return object;
    }

    @Override
    public Class<?>[] type() {
        return new Class[] {FilteredListener.class, Listener.class};
    }
}
