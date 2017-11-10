package com.dyn.schematics;

import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistrationHandler {
	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		event.getRegistry().register(SchematicMod.schematicBlockStand);
		event.getRegistry().register(SchematicMod.schematicBlockWall);
		RegistrationHandler.registerTileEntities();
	}

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().register(SchematicMod.schematic);
	}

	private static void registerTileEntities() {
		GameRegistry.registerTileEntity(ClaimBlockTileEntity.class, "schem_block_te");
	}
}