package org.imanity.framework.discord.input;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.jetbrains.annotations.NotNull;

@Component
public class InputListener extends ListenerAdapter {

    @Autowired
    private InputService inputService;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        this.inputService.handle(event);
    }
}
