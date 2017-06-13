package com.dyn.schematics;

import org.apache.logging.log4j.Logger;

import com.dyn.schematics.proxy.Proxy;
import com.dyn.schematics.reference.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class SchematicMod {
	@Mod.Instance(Reference.MOD_ID)
	public static SchematicMod instance;

	public static Logger logger;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static Item schematic;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		
		proxy.preInit();
		
		proxy.registerItem(schematic = new ItemSchematic(), "schematic");
	}
}
