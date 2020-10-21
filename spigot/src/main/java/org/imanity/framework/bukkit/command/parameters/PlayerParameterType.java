package org.imanity.framework.bukkit.command.parameters;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.util.CC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PlayerParameterType extends BukkitParameterHolder<Player> {

	@Override
	public Player transform(final CommandSender sender, final String source) {
		if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals("")))
			return ((Player) sender);

		final Player player = Bukkit.getServer().getPlayer(source);

		if (player == null) {
			sender.sendMessage(CC.RED + "No player with the name " + source + " found.");
			return (null);
		}

		return (player);
	}

	@Override
	public List<String> tabComplete(final Player sender, final Set<String> flags, final String source) {
		final List<String> completions = new ArrayList<>();

		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (StringUtils.startsWithIgnoreCase(player.getName(), source)) {
				completions.add(player.getName());
			}
		}

		return completions;
	}

	@Override
	public Class[] type() {
		return new Class[] {Player.class};
	}
}