package com.dyn.schematics.proxy;

import com.rabbit.gui.base.Stage;
import com.rabbit.gui.show.IShow;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Server implements Proxy {

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerItem(Item item, String name) {
		item.setUnlocalizedName(name);
		GameRegistry.registerItem(item, name);
	}

	@Override
	public void registerItemModels(Item item, String name, int meta) {
	}


}