package com.dyn.schematics.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dyn.schematics.reference.Reference;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Client implements Proxy {

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
		File schematicLocs = new File(Minecraft.getMinecraft().mcDataDir, "schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}

	@Override
	public void registerItem(Item item, String name) {
		GameRegistry.registerItem(item, name);
		item.setUnlocalizedName(name);
		List<ItemStack> list = new ArrayList<>();
		item.getSubItems(item, null, list);
		for (ItemStack stack : list) {
			registerItemModels(item, item.getUnlocalizedName(stack), stack.getItemDamage());
		}
	}

	@Override
	public void registerItemModels(Item item, String name, int meta) {
		if (name.contains("item.")) {
			name = name.replace("item.", "");
		}
		ModelResourceLocation location = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, meta, location);
	}
}