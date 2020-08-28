package org.imanity.framework.bukkit.visual;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class VisualBlockData extends MaterialData {
	public VisualBlockData(final Material type) {
		super(type);
	}

	public VisualBlockData(final Material type, final byte data) {
		super(type, data);
	}

	public Material getBlockType() {
		return getItemType();
	}

	@Override
	@Deprecated
	public Material getItemType() {
		return super.getItemType();
	}

	@Override
	@Deprecated
	public ItemStack toItemStack() {
		throw new UnsupportedOperationException("This is a VisualBlock data");
	}

	@Override
	@Deprecated
	public ItemStack toItemStack(final int amount) {
		throw new UnsupportedOperationException("This is a VisualBlock data");
	}
}
