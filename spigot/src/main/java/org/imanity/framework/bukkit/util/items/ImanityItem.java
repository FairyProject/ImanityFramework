package org.imanity.framework.bukkit.util.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.LocaleRV;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.util.nms.NBTEditor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ImanityItem {

    private static final Int2ObjectMap<ImanityItem> REGISTERED_ITEM = new Int2ObjectOpenHashMap<>();
    private static final AtomicInteger ITEM_COUNTER = new AtomicInteger();

    public static ImanityItem getItem(int id) {
        return REGISTERED_ITEM.get(id);
    }

    @Nullable
    public static ImanityItem getItemFromBukkit(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        if (!NBTEditor.contains(itemStack, "imanity", "item", "id")) {
            return null;
        }

        String value = NBTEditor.getString(itemStack, "imanity", "item", "id");

        if (value == null) {
            return null;
        }

        int id = Integer.parseInt(value);
        return REGISTERED_ITEM.get(id);
    }

    private int id;
    private ItemBuilder itemBuilder;
    private String displayNameLocale;
    private String displayLoreLocale;

    private final List<LocaleRV> displayNamePlaceholders = new ArrayList<>();
    private final List<LocaleRV> displayLorePlaceholders = new ArrayList<>();

    public ImanityItem item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ImanityItem displayNameLocale(String locale) {
        this.displayNameLocale = locale;
        return this;
    }

    public ImanityItem displayLoreLocale(String locale) {
        this.displayLoreLocale = locale;
        return this;
    }

    public ImanityItem appendNameReplace(String target, Function<Player, String> replacement) {
        this.displayNamePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    public ImanityItem appendLoreReplace(String target, Function<Player, String> replacement) {
        this.displayLorePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    public ImanityItem submit() {

        this.id = ITEM_COUNTER.getAndIncrement();
        REGISTERED_ITEM.put(this.id, this);

        return this;

    }

    public ItemStack build(Player receiver) {
        ItemBuilder itemBuilder = this.itemBuilder.clone();

        String name = Imanity.translate(receiver, displayNameLocale);
        for (LocaleRV rv : this.displayNamePlaceholders) {
            name = Utility.replace(name, rv.getTarget(), rv.getReplacement(receiver));
        }

        itemBuilder.name(name);

        String lore = Imanity.translate(receiver, displayLoreLocale);
        for (LocaleRV rv : this.displayLorePlaceholders) {
            lore = Utility.replace(lore, rv.getTarget(), rv.getReplacement(receiver));
        }

        return itemBuilder
                .name(name)
                .lore(Utility.toStringList(lore, "\n"))
                .tag(this.id, "imanity", "item", "id")
                .build();
    }
}