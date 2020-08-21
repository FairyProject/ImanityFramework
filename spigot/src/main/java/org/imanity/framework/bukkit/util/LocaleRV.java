package org.imanity.framework.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.imanity.framework.bukkit.Imanity;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class LocaleRV {

    private final String target;
    private final Function<Player, String> replacement;

    public LocaleRV(String target, String replacement) {
        this.target = target;
        this.replacement = player -> replacement;
    }

    public LocaleRV(String target, Object replacement) {
        this(target, replacement.toString());
    }

    public String getReplacement(Player player) {
        return this.replacement.apply(player);
    }

    public static LocaleRV o(final String target, final String replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV o(final String target, final Object replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV o(String target, Function<Player, String> replacement) {
        return new LocaleRV(target, replacement);
    }

    public static LocaleRV oT(String target, String locale) {
        return new LocaleRV(target, player -> Imanity.translate(player, locale));
    }

}
