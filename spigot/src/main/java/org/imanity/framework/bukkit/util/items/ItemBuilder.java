package org.imanity.framework.bukkit.util.items;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.imanity.framework.bukkit.util.nms.NBTEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemBuilder implements Listener, Cloneable {

	private ItemStack itemStack;

	public ItemBuilder(final Material mat) {
		itemStack = new ItemStack(mat);
	}

	public ItemBuilder(final ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemBuilder amount(final int amount) {
		itemStack.setAmount(amount);
		return this;
	}

	public ItemBuilder name(final String name) {
		final ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final Iterable<String> lore) {
		final ItemMeta meta = itemStack.getItemMeta();

		List<String> toSet = meta.getLore();
		if (toSet == null) {
			toSet = new ArrayList<>();
		}

		for (final String string : lore) {
			toSet.add(ChatColor.translateAlternateColorCodes('&', string));
		}

		meta.setLore(toSet);
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final String... lore) {
		final ItemMeta meta = itemStack.getItemMeta();

		List<String> toSet = meta.getLore();
		if (toSet == null) {
			toSet = new ArrayList<>();
		}

		for (final String string : lore) {
			toSet.add(ChatColor.translateAlternateColorCodes('&', string));
		}

		meta.setLore(toSet);
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder durability(final int durability) {
		itemStack.setDurability((short) durability);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder data(final int data) {
		itemStack.setData(new MaterialData(itemStack.getType(), (byte) data));
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
		itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment) {
		itemStack.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder type(final Material material) {
		itemStack.setType(material);
		return this;
	}

	public ItemBuilder clearLore() {
		final ItemMeta meta = itemStack.getItemMeta();
		meta.setLore(new ArrayList<String>());
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearEnchantments() {
		for (final Enchantment e : itemStack.getEnchantments().keySet()) {
			itemStack.removeEnchantment(e);
		}
		return this;
	}

	public ItemBuilder color(final Color color) {
		if (itemStack.getType() == Material.LEATHER_BOOTS || itemStack.getType() == Material.LEATHER_CHESTPLATE || itemStack.getType() == Material.LEATHER_HELMET
				|| itemStack.getType() == Material.LEATHER_LEGGINGS) {
			final LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
			meta.setColor(color);
			itemStack.setItemMeta(meta);
			return this;
		} else
			throw new IllegalArgumentException("color() only applicable for leather armor!");
	}

	public ItemBuilder tag(Object value, String... key) {
		this.itemStack = NBTEditor.set(this.itemStack, value, key);
		return this;
	}

	@Override
	public ItemBuilder clone() {
		return new ItemBuilder(this.itemStack.clone());
	}

	public ItemStack build() {
		return itemStack;
	}

}