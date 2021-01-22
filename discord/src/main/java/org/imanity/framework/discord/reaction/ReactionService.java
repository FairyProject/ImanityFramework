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

package org.imanity.framework.discord.reaction;

import org.imanity.framework.Autowired;
import org.imanity.framework.PostInitialize;
import org.imanity.framework.PreInitialize;
import org.imanity.framework.Service;
import org.imanity.framework.discord.DiscordService;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "reactionMessage", dependencies = "discord")
public class ReactionService {

    public static ReactionService INSTANCE;

    private Map<Long, ReactionMessage> messages;

    @Autowired
    private DiscordService discordService;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;
    }

    @PostInitialize
    public void init() {
        this.messages = new ConcurrentHashMap<>();
    }

    @Nullable
    public ReactionMessage findMessage(long id) {
        return this.messages.getOrDefault(id, null);
    }

    public void send(ReactionMessage message) {
        this.messages.put(message.getMessage().getMessageId(), message);
    }

    public void delete(long id) {
        this.messages.remove(id);
    }

}
