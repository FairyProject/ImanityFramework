package org.imanity.framework.bukkit.player.locale;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.LocaleRV;
import org.imanity.framework.bukkit.util.BukkitUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LocaleMessage {

    private String localeName;
    private List<LocaleRV> replaceValues;

    public LocaleMessage(String localeName) {
        this.localeName = localeName;
        this.replaceValues = new ArrayList<>();
    }

    public LocaleMessage appendReplacement(String target, String replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
        return this;
    }

    public LocaleMessage appendReplacement(String target, Object replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
        return this;
    }

    public LocaleMessage appendReplacement(String target, Function<Player, String> replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
        return this;
    }

    public LocaleMessage appendLocaleReplacement(String target, String localeReplacement) {
        this.replaceValues.add(LocaleRV.oT(target, localeReplacement));
        return this;
    }

    public void send(Player player) {
        String result = Imanity.translate(player, this.localeName);

        for (LocaleRV rv : this.replaceValues) {
            result = BukkitUtil.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        player.sendMessage(result);
    }

}
