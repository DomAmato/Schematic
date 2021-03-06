package com.dyn.schematics.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.dyn.schematics.Schematic;
import com.google.common.collect.Lists;

import net.minecraft.nbt.CompressedStreamTools;

public class SchematicRegistry {

	private static List<File> schematicLocations = Lists.newArrayList();

	public static void addSchematicLocation(File loc) {
		SchematicRegistry.schematicLocations.add(loc);
	}

	public static List<String> enumerateSchematics() {
		List<String> schematics = Lists.newArrayList();
		for (File location : SchematicRegistry.schematicLocations) {
			for (File schem : location.listFiles((FilenameFilter) (dir, name) -> name.endsWith("schematic"))) {
				schematics.add(schem.getName().replace(".schematic", ""));
			}
		}
		return schematics;
	}

	public static List<File> getSchematicLocations() {
		return SchematicRegistry.schematicLocations;
	}

	public static Schematic load(String name) {
		InputStream stream = null;
		for (File location : SchematicRegistry.schematicLocations) {
			try {
				stream = new FileInputStream(new File(location, name + ".schematic"));
			} catch (FileNotFoundException e2) {
				continue;
			}
			break;
		}
		if (stream != null) {
			try {
				Schematic schema = new Schematic(name);
				schema.readFromNBT(CompressedStreamTools.readCompressed(stream));
				stream.close();
				return schema;
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}
}
