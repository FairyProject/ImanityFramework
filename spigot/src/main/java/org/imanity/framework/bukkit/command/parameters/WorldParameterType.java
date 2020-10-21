package org.imanity.framework.bukkit.command.parameters;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.plugin.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class WorldParameterType extends BukkitParameterHolder<World> {

	@Override
	public World transform(final CommandSender sender, final String source) {
		final World world = Bukkit.getServer().getWorld(source);

		if (world == null) {
			sender.sendMessage(ChatColor.RED + "No world with the name " + source + " found.");
			return (null);
		}

		return (world);
	}

	@Override
	public List<String> tabComplete(final Player sender, final Set<String> flags, final String source) {
		final List<String> completions = new ArrayList<>();

		for (final World world : Bukkit.getServer().getWorlds()) {
			if (StringUtils.startsWithIgnoreCase(world.getName(), source)) {
				completions.add(world.getName());
			}
		}

		return (completions);
	}

	@Override
	public Class[] type() {
		return new Class[] {World.class};
	}
}