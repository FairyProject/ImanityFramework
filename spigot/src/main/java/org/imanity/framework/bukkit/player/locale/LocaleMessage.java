package org.imanity.framework.bukkit.player.locale;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.LocaleRV;
import org.imanity.framework.bukkit.util.RV;
import org.imanity.framework.bukkit.util.Utility;

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

    public void appendReplacement(String target, String replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
    }

    public void appendReplacement(String target, Object replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
    }

    public void appendReplacement(String target, Function<Player, String> replacement) {
        this.replaceValues.add(LocaleRV.o(target, replacement));
    }

    public void appendLocaleReplacement(String target, String localeReplacement) {
        this.replaceValues.add(LocaleRV.oT(target, localeReplacement));
    }

    public void send(Player player) {
        String result = Imanity.translate(player, this.localeName);

        for (LocaleRV rv : this.replaceValues) {
            result = Utility.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        player.sendMessage(result);
    }

}
