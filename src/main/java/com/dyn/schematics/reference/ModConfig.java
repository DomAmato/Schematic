package com.dyn.schematics.reference;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = Reference.MOD_ID)
public class ModConfig {
	public static class ConfigOptions {

		@Comment("Allow players to build from schematics on the server")
		@Name("Allow Building")
		public boolean can_build = true;

		@Comment("Building schematics in non-creative mode requires the players to have the necessary ingredients")
		@Name("Require Resources")
		public boolean req_resources = true;

		@Comment("Require the exact ingredients, by default the mod allows you to use any block with the same data value to satisfy a reqirement (e.g. stone, diorite, granite etc will work for all stone)")
		@Name("Require Exact Resources")
		public boolean req_exact = false;

		@Comment("Allow schematics on the client machine to appear in the architect desk")
		@Name("Allow schematics from client")
		public boolean can_use_client_schematic = true;

		@Comment("The max number of blocks renderable by a schematic, this drops the framerate when very large schematics are rendered (client only)")
		@Name("Max Render Size")
		@RangeInt(min = 100, max = 500000)
		public int max_size = 100000;
	}

	private static final ConfigOptions localConfig = new ConfigOptions();

	public static final ConfigOptions config = new ConfigOptions();

	private static boolean useLocalConfig = false;

	public static ConfigOptions getConfig() {
		return ModConfig.useLocalConfig ? ModConfig.localConfig : ModConfig.config;
	}

	public static void readConfigFromNBT(NBTTagCompound compound) {
		ModConfig.config.can_build = compound.getBoolean("build");
		ModConfig.config.can_use_client_schematic = compound.getBoolean("client");
		ModConfig.config.req_resources = compound.getBoolean("resource");
		ModConfig.config.req_exact = compound.getBoolean("exact");
	}

	public static void setUseLocalConfig(boolean state) {
		ModConfig.useLocalConfig = state;
	}

	public static void syncLocalConfig() {
		ModConfig.localConfig.can_build = ModConfig.config.can_build;
		ModConfig.localConfig.can_use_client_schematic = ModConfig.config.can_use_client_schematic;
		ModConfig.localConfig.req_resources = ModConfig.config.req_resources;
		ModConfig.localConfig.req_exact = ModConfig.config.req_exact;
		ModConfig.localConfig.max_size = ModConfig.config.max_size;
	}

	public static NBTTagCompound writeConfigToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("build", ModConfig.config.can_build);
		compound.setBoolean("client", ModConfig.config.can_use_client_schematic);
		compound.setBoolean("resource", ModConfig.config.req_resources);
		compound.setBoolean("exact", ModConfig.config.req_exact);
		return compound;

	}
}