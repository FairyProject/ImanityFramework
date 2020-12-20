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

package org.imanity.framework.bukkit.listener;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
public class FilteredEventList {

    private final Predicate<Event>[] filters;

    private FilteredEventList(Builder builder) {
        this.filters = builder.filters.toArray(new Predicate[0]);
    }

    public boolean check(Event event) {

        for (Predicate<Event> filter : this.filters) {
            if (!filter.test(event)) {
                return false;
            }
        }

        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Predicate<Event>> filters;

        public Builder() {
            this.filters = new ArrayList<>(1);
        }

        public Builder filter(Predicate<Event> filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder filter(BiPredicate<Player, Event> filter) {
            this.filters.add(event -> {
                Player player = null;
                if (event instanceof PlayerEvent) {
                    player = ((PlayerEvent) event).getPlayer();
                } else {
                    try { // LeeGod - use MethodResolver so it caches methods
                        MethodResolver methodResolver = new MethodResolver(event.getClass());
                        MethodWrapper<Player> methodWrapper = methodResolver.resolve(Player.class, 0);

                        if (methodWrapper.exists()) {
                            player = methodWrapper.invoke(event);
                        }
                    } catch (Exception ex) {
                    }
                }

                if (player != null) {
                    return filter.test(player, event);
                }
                return true;
            });

            return this;
        }

        public FilteredEventList build() {
            return new FilteredEventList(this);
        }

    }

}
