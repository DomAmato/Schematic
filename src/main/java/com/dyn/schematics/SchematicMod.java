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
import com.dyn.schematics.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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

	private static Configuration configFile;

	public static boolean can_build;

	public static boolean req_resources;

	public static int max_size;

	public static boolean integrated = true;

	public static void synchronizeConfig() {
		SchematicMod.can_build = SchematicMod.configFile.getBoolean("Allow Building", Configuration.CATEGORY_GENERAL,
				true, "Allow players to build from schematics on the server");
		SchematicMod.req_resources = SchematicMod.configFile.getBoolean("Require Resources",
				Configuration.CATEGORY_GENERAL, true,
				"Building schematics in non-creative mode requires the players to have the necessary ingredients");
		SchematicMod.max_size = SchematicMod.configFile.getInt("Max Render Size", Configuration.CATEGORY_GENERAL,
				100000, 100, 500000,
				"The max number of blocks renderable by a schematic, this drops the framerate when very large schematics are rendered (client only)");
		if (SchematicMod.configFile.hasChanged()) {
			SchematicMod.configFile.save();
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		SchematicMod.proxy.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
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

		SchematicMod.configFile = new Configuration(event.getSuggestedConfigurationFile());
		SchematicMod.configFile.load();
		SchematicMod.synchronizeConfig();
	}

	@Mod.EventHandler
	public void serverstart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuildSchematic());
		event.registerServerCommand(new CommandSaveSchematic());
		event.registerServerCommand(new CommandLoadSchematic());
	}
}
