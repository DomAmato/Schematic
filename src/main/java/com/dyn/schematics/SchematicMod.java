package com.dyn.schematics;

import org.apache.logging.log4j.Logger;

import com.dyn.schematics.block.BlockArchitectDesk;
import com.dyn.schematics.block.BlockSchematicClaimStand;
import com.dyn.schematics.block.BlockSchematicClaimWall;
import com.dyn.schematics.commands.CommandBuildSchematic;
import com.dyn.schematics.commands.CommandLoadSchematic;
import com.dyn.schematics.commands.CommandSaveSchematic;
import com.dyn.schematics.gui.GuiHandler;
import com.dyn.schematics.item.ItemSchematic;
import com.dyn.schematics.network.NetworkManager;
import com.dyn.schematics.proxy.Proxy;
import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.reference.Reference;
import com.dyn.schematics.utils.SchematicsTab;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = "@VERSION")
public class SchematicMod {

	@Mod.Instance(Reference.MOD_ID)
	public static SchematicMod instance;

	public static Logger logger;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static final SchematicsTab schemTab = new SchematicsTab();

	public static final Item schematic = new ItemSchematic();

	public static final Block schematicBlockStand = new BlockSchematicClaimStand();
	public static final Block schematicBlockWall = new BlockSchematicClaimWall();
	public static final BlockArchitectDesk desk = new BlockArchitectDesk();

	public static BlockPos startPos = BlockPos.ORIGIN;

	public static BlockPos endPos = BlockPos.ORIGIN;

	public static boolean integrated = true;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		SchematicMod.proxy.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		ConfigManager.sync(Reference.MOD_ID, Type.INSTANCE);
		ModConfig.syncLocalConfig();
	}

	@SubscribeEvent
	public void onConfigChangedEvent(OnConfigChangedEvent event) {
		if (event.getModID().equals(Reference.MOD_ID)) {
			ConfigManager.sync(Reference.MOD_ID, Type.INSTANCE);
			ModConfig.syncLocalConfig();
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		SchematicMod.logger = event.getModLog();
		SchematicMod.proxy.preInit();

		try {
			Class.forName("net.minecraft.client.Minecraft");
		} catch (ClassNotFoundException e) {
			SchematicMod.integrated = false;
		}

		NetworkManager.registerMessages();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void serverstart(FMLServerStartingEvent event) {
		ModConfig.setUseLocalConfig(true);
		event.registerServerCommand(new CommandBuildSchematic());
		event.registerServerCommand(new CommandSaveSchematic());
		event.registerServerCommand(new CommandLoadSchematic());
	}
}
