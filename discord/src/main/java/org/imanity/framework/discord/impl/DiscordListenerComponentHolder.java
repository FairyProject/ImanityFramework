package org.imanity.framework.discord.impl;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.discord.DiscordService;
import org.imanity.framework.plugin.component.ComponentHolder;

public class DiscordListenerComponentHolder extends ComponentHolder {
    @Override
    public Class<?>[] type() {
        return new Class[] {ListenerAdapter.class};
    }

    @Override
    public Object newInstance(Class<?> type) {
        Object object = super.newInstance(type);
        ImanityCommon.getService(DiscordService.class).getJda().addEventListener(object);

        return object;
    }
}
