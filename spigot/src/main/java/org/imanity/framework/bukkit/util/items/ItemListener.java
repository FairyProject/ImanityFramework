package org.imanity.framework.bukkit.util.items;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.plugin.component.Component;

@Component
public class ItemListener implements Listener {

    private static final String SET_ITEM_METADATA = ImanityCommon.METADATA_PREFIX + "SetItem";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        Item item = event.getItem();

        if (item.hasMetadata(SET_ITEM_METADATA)) {
            return;
        }

        ItemStack itemStack = item.getItemStack();
        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        ItemStack resultItem = imanityItem.build(player);
        resultItem.setAmount(itemStack.getAmount());
        resultItem.setDurability(itemStack.getDurability());

        item.setItemStack(resultItem);
        item.setMetadata(SET_ITEM_METADATA, new SampleMetadata(SET_ITEM_METADATA));
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            return;
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        if (imanityItem.getClickCallback() != null &&
                !imanityItem.getClickCallback().onClick(player, itemStack, action)) {
            event.setCancelled(true);
        }
    }

}
