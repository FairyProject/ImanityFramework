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

package org.imanity.framework.discord.command.parameters;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.imanity.framework.Component;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.discord.DiscordService;

import java.util.regex.Matcher;

@Component
public class MemberParameterHolder implements ParameterHolder<Member> {

    @Override
    public Class[] type() {
        return new Class[] {Member.class};
    }

    @Override
    public Member transform(CommandEvent commandEvent, String source) {
        Guild guild = DiscordService.INSTANCE.getGuild();
        if (guild == null) {
            return null;
        }

        Matcher matcher = Message.MentionType.USER.getPattern().matcher(source);
        if (!matcher.find()) {
            return null;
        }
        long id = Long.parseLong(matcher.group(1));

        return guild.getMemberById(id);
    }
}
