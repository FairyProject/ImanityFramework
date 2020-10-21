package org.imanity.framework.bukkit.command.parameters;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class BukkitParameterHolder<T> implements ParameterHolder<T> {

    @Override
    public final T transform(InternalCommandEvent commandEvent, String source) {
        if (commandEvent.getUser() instanceof CommandSender) {
            return this.transform((CommandSender) commandEvent.getUser(), source);
        }

        throw new UnsupportedOperationException();
    }

    public abstract T transform(CommandSender sender, String source);

    public final List<String> tabComplete(Object user, Set<String> flags, String source) {
        if (user instanceof Player) {
            return this.tabComplete((Player) user, flags, source);
        }

        throw new UnsupportedOperationException();
    }

    public abstract List<String> tabComplete(Player player, Set<String> flags, String source);

}
