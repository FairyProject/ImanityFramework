package org.imanity.framework.bukkit.util.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        event.setCancelled(true);
        item.remove();

        ItemStack resultItem = imanityItem.build(player);
        resultItem.setAmount(itemStack.getAmount());
        resultItem.setDurability(itemStack.getDurability());

        player.getInventory().addItem(resultItem);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
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
