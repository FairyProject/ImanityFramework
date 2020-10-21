package org.imanity.framework.discord.activity;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Activity;

@Getter
public abstract class ActivityProvider {

    private final int priority;

    public ActivityProvider(int priority) {
        this.priority = priority;
    }

    public abstract Activity activity();

}
