package org.imanity.framework.bukkit.visibility;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.imanity.framework.bukkit.events.player.PlayerPostJoinEvent;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

import java.util.Collection;

@Component
public class VisibilityListener implements Listener {

    @Autowired
    private VisibilityService visibilityService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerPostJoinEvent event) {
        this.visibilityService.update(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTabComplete(PlayerChatTabCompleteEvent event) {
        final String token = event.getLastToken();
        final Collection<String> completions = (Collection<String>)event.getTabCompletions();
        completions.clear();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (!this.visibilityService.treatAsOnline(target, event.getPlayer())) {
                continue;
            }
            if (!StringUtils.startsWithIgnoreCase(target.getName(), token)) {
                continue;
            }
            completions.add(target.getName());
        }
    }
}
