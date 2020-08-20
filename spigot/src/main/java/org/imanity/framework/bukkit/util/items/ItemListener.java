package org.imanity.framework.bukkit.util.items;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        ItemStack resultItem = imanityItem.build(player);
        resultItem.setAmount(itemStack.getAmount());
        resultItem.setDurability(itemStack.getDurability());

        player.getInventory().addItem(resultItem);
        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
    }

}
