package org.imanity.framework.bukkit.yaml;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.util.items.ImanityItem;
import org.imanity.framework.bukkit.util.items.ItemBuilder;
import org.imanity.framework.config.annotation.ConfigurationElement;
import org.imanity.framework.config.annotation.IgnoredElement;
import org.imanity.framework.locale.Locale;

import java.util.HashMap;
import java.util.Map;

@ConfigurationElement
public class ItemStackElement {

    private Material MATERIAL = Material.AIR;
    private short DATA = 0;

    private String IDENTITY_NAME = "none";

    private Map<String, String> DISPLAY_NAME_LOCALES = new HashMap<>();
    private Map<String, String> DISPLAY_LORE_LOCALES = new HashMap<>();

    @IgnoredElement private ImanityItem item;

    public void register() {

        for (Map.Entry<String, String> entry : DISPLAY_NAME_LOCALES.entrySet()) {

            Locale locale = ImanityCommon.LOCALE_HANDLER.getOrRegister(entry.getKey());
            locale.registerEntry(this.getLocaleName(), entry.getValue());

        }

        for (Map.Entry<String, String> entry : DISPLAY_LORE_LOCALES.entrySet()) {

            Locale locale = ImanityCommon.LOCALE_HANDLER.getOrRegister(entry.getKey());
            locale.registerEntry(this.getLocaleLore(), entry.getValue());

        }

        this.item = new ImanityItem()
                .item(new ItemBuilder(MATERIAL)
                        .durability(DATA))
                .displayNameLocale(this.getLocaleName())
                .displayLoreLocale(this.getLocaleLore())
                .submit();

    }

    public String getLocaleName() {
        return "item." + IDENTITY_NAME + ".name";
    }

    public String getLocaleLore() {
        return "item." + IDENTITY_NAME + ".lore";
    }


    public ImanityItem toImanityItem(Player player) {
        if (this.item == null) {
            this.register();
        }
        return this.item;
    }

}
