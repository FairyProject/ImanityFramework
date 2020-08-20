package org.imanity.framework.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionEventChecker {

    private Supplier<Boolean> nonPlayerChecker;
    private Function<Player, Boolean> playerChecker;
    private Map<Class<? extends Event>, Function<Event, Player>> specialGetPlayer = new HashMap<>();

    public FunctionEventChecker playerOnly(Function<Player, Boolean> function) {
        this.playerChecker = function;
        return this;
    }

    public FunctionEventChecker nonPlayerOnly(Supplier<Boolean> function) {
        this.nonPlayerChecker = function;
        return this;
    }

    public FunctionEventChecker getPlayer(Class<? extends Event> eventClass, Function<Event, Player> function) {
        this.specialGetPlayer.put(eventClass, function);
        return this;
    }

    public boolean check(Event event) {
        Player player = getPlayerFromEvent(event);
        if (player != null && playerChecker != null) {
            return playerChecker.apply(player);
        }
        if (nonPlayerChecker != null) {
            return nonPlayerChecker.get();
        }
        return true;
    }

    private Player getPlayerFromEvent(Event event) {
        if (specialGetPlayer.containsKey(event.getClass())) {
            return specialGetPlayer.get(event.getClass()).apply(event);
        }
        if (event instanceof PlayerEvent) {
            return ((PlayerEvent) event).getPlayer();
        }
        try {
            for (Method method : event.getClass().getDeclaredMethods()) {
                if (Player.class.isAssignableFrom(method.getReturnType())) {
                    method.setAccessible(true);
                    return (Player) method.invoke(event);
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

}
