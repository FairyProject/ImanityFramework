package org.imanity.framework.bukkit.util.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.LocaleRV;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.util.nms.NBTEditor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
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
    private boolean submitted;
    private ItemBuilder itemBuilder;
    private String displayNameLocale;
    private String displayLoreLocale;

    private ItemCallback clickCallback;

    private final List<LocaleRV> displayNamePlaceholders = new ArrayList<>();
    private final List<LocaleRV> displayLorePlaceholders = new ArrayList<>();

    private final Map<String, Object> metadata = new HashMap<>();

    public  Object getMetadata(String key) {
        return this.metadata.get(key);
    }

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

    public ImanityItem callback(ItemCallback callback) {
        this.clickCallback = callback;
        return this;
    }

    public ImanityItem metadata(String key, Object object) {
        this.metadata.put(key, object);
        return this;
    }

    public ImanityItem submit() {

        this.id = ITEM_COUNTER.getAndIncrement();
        REGISTERED_ITEM.put(this.id, this);

        this.submitted = true;

        return this;

    }

    public Material getType() {
        return this.itemBuilder.getType();
    }

    public ItemStack build(Player receiver) {
        ItemBuilder itemBuilder = this.itemBuilder.clone();

        if (displayNameLocale != null) {
            String name = Imanity.translate(receiver, displayNameLocale);
            for (LocaleRV rv : this.displayNamePlaceholders) {
                name = Utility.replace(name, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.name(name);
        }

        if (displayLoreLocale != null) {
            String lore = Imanity.translate(receiver, displayLoreLocale);
            for (LocaleRV rv : this.displayLorePlaceholders) {
                lore = Utility.replace(lore, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.lore(Utility.toStringList(lore, "\n"));

        }

        if (!this.submitted) {
            return itemBuilder.build();
        }
        return itemBuilder
                .tag(this.id, "imanity", "item", "id")
                .build();
    }
}