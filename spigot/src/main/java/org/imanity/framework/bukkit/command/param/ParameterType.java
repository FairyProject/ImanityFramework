package org.imanity.framework.bukkit.command.param;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface ParameterType<T> {

	T transform(CommandSender sender, String source);

	List<String> tabComplete(Player sender, Set<String> flags, String source);

	default List<String> tabComplete(Player sender, String[] parameters, Set<String> flags, String source) {
		return this.tabComplete(sender, flags, source);
	}

}