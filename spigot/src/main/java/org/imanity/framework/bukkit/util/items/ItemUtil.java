package org.imanity.framework.bukkit.util.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtil {

    public static String toString(ItemStack itemStack) {
        if(itemStack == null) itemStack = new ItemStack(Material.AIR);
        StringBuilder enchantments = new StringBuilder();
        String comma = "";
        for(Map.Entry<Enchantment, Integer> e : itemStack.getEnchantments().entrySet()) {
            enchantments.append(comma);
            enchantments.append(e.getKey().getName()).append("-").append(e.getValue());
            comma = ";";
        }
        if(!enchantments.toString().equals("")) {
            return itemStack.getType() + "/" + itemStack.getDurability() + "/" + itemStack.getAmount() + "/" + enchantments;
        }
        return itemStack.getType() + "/" + itemStack.getDurability() + "/" + itemStack.getAmount();
    }

    public static ItemStack fromString(String string) {
        try{
            String[] s = string.split("/");
            if(!(s.length >= 3)) {
                return new ItemStack(Material.AIR);
            }
            ItemStack is = new ItemStack(Material.getMaterial(s[0]));
            is.setDurability(Short.parseShort(s[1]));
            is.setAmount(Integer.parseInt(s[2]));
            if(s.length > 3) {
                for(String enchantment : s[3].split(";")) {
                    String[] ench = enchantment.split("-");
                    is.addUnsafeEnchantment(Enchantment.getByName(ench[0]), Integer.parseInt(ench[1]));
                }
            }
            return is;
        } catch (Exception e) {}
        return new ItemStack(Material.AIR);
    }

}
