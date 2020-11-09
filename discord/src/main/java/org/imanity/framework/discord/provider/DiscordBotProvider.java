package org.imanity.framework.discord.provider;

import net.dv8tion.jda.api.JDABuilder;

public interface DiscordBotProvider {

    void setupBot(JDABuilder builder);

}
