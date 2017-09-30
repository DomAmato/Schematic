package com.dyn.schematics;

import org.apache.logging.log4j.Logger;

import com.dyn.schematics.commands.CommandBuildSchematic;
import com.dyn.schematics.commands.CommandLoadSchematic;
import com.dyn.schematics.commands.CommandSaveSchematic;
import com.dyn.schematics.proxy.Proxy;
import com.dyn.schematics.reference.Reference;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class SchematicMod {
	@Mod.Instance(Reference.MOD_ID)
	public static SchematicMod instance;

	public static Logger logger;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static final Item schematic = new ItemSchematic();

	public static BlockPos startPos = BlockPos.ORIGIN;
	public static BlockPos endPos = BlockPos.ORIGIN;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		proxy.preInit();
	}
	
	@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
	public static class RegistrationHandler {
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			event.getRegistry().register(schematic);
		}
	}
	
	@Mod.EventHandler
	public void serverstart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuildSchematic());
		event.registerServerCommand(new CommandSaveSchematic());
		event.registerServerCommand(new CommandLoadSchematic());
	}
}
