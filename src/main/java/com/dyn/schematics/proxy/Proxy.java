package com.dyn.schematics.proxy;

import net.minecraft.item.Item;

public interface Proxy {
	public void init();

	public void postInit();

	public void preInit();

	public void registerItem(Item item, String name);

	public void registerItemModels(Item item, String name, int meta);
}