/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
