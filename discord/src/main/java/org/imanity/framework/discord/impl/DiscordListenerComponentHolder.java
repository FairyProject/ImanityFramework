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

package org.imanity.framework.discord.impl;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.imanity.framework.ComponentHolder;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.discord.DiscordService;

public class DiscordListenerComponentHolder extends ComponentHolder {
    @Override
    public Class<?>[] type() {
        return new Class[] {ListenerAdapter.class};
    }

    @Override
    public Object newInstance(Class<?> type) {
        Object object = super.newInstance(type);
        DiscordService discordService = ImanityCommon.getBean(DiscordService.class);

        discordService.registerListener((ListenerAdapter) object);

        return object;
    }
}
