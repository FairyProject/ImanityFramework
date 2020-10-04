package org.imanity.framework.bukkit.listener;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public class FunctionEventChecker {

    private Supplier<Boolean> nonPlayerChecker;
    private Function<Player, Boolean> playerChecker;

    private Map<Class<? extends Event>, Function<Event, Player>> specialGetPlayer = new HashMap<>();

    public FunctionEventChecker playerOnly(Function<Player, Boolean> function) {
        return this.playerOnly(function, false);
    }

    public FunctionEventChecker playerOnly(Function<Player, Boolean> function, boolean includingNonPlayerCheck) {
        if (includingNonPlayerCheck) {
            this.playerChecker = player -> function.apply(player) && nonPlayerChecker.get();
        } else {
            this.playerChecker = function;
        }
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
        try { // LeeGod - use MethodResolver so it caches methods
            MethodResolver methodResolver = new MethodResolver(event.getClass());
            MethodWrapper<Player> methodWrapper = methodResolver.resolve(Player.class, 0);

            return methodWrapper.exists() ? methodWrapper.invoke(event) : null;
        } catch (Exception ex) {
            return null;
        }
    }

}
