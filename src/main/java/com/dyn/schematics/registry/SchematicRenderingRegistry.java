package com.dyn.schematics.registry;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.renderer.SchematicRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SchematicRenderingRegistry {
	private static Map<String, Triple<Schematic, BlockPos, Integer>> compiledSchematics = Maps.newHashMap();
	private static Map<Schematic, Pair<Integer, BlockPos>> compiledDisplayListId = Maps.newHashMap();
	private static List<Triple<Schematic, BlockPos, Integer>> toCompile = Lists.newArrayList();

	public static void addSchematic(Schematic schematic, BlockPos offset, int rotation) {
		if (schematic.size < 125000) {
			if (compiledSchematics.containsKey(schematic.name)) {
				// if we already compiled this schematic with the same offset
				// and rotation dont add it again
				if ((compiledSchematics.get(schematic.name).getMiddle() != offset)
						|| (compiledSchematics.get(schematic.name).getRight() != rotation)) {
					toCompile.add(new ImmutableTriple<>(schematic, offset, rotation));
					// delete the display list associated with the schematic
					GLAllocation.deleteDisplayLists(
							compiledDisplayListId.get(compiledSchematics.get(schematic.name).getLeft()).getLeft());
					compiledSchematics.remove(schematic.name);
				}
			} else {
				toCompile.add(new ImmutableTriple<>(schematic, offset, rotation));
			}
		}
	}

	public static boolean containsCompiledSchematic(Schematic schematic) {
		return compiledSchematics.containsKey(schematic.name);
	}

	public static boolean containsCompiledSchematic(Schematic schematic, BlockPos pos) {
		if (compiledSchematics.containsKey(schematic.name)) {
			return compiledSchematics.get(schematic.name).getMiddle().equals(pos);
		}
		return false;
	}

	public static boolean containsCompiledSchematic(Schematic schematic, BlockPos pos, int rotation) {
		if (compiledSchematics.containsKey(schematic.name)) {
			return compiledSchematics.get(schematic.name).getMiddle().equals(pos)
					&& (compiledSchematics.get(schematic.name).getRight() == rotation);
		}
		return false;
	}

	public static void removeSchematic(Schematic schematic) {
		if (compiledSchematics.containsKey(schematic.name)) {
			GLAllocation.deleteDisplayLists(
					compiledDisplayListId.get(compiledSchematics.get(schematic.name).getLeft()).getLeft());
			compiledDisplayListId.remove(compiledSchematics.remove(schematic.name).getLeft());
		}
	}

	public static void rotateSchematic(Schematic schematic) {
		if (compiledSchematics.containsKey(schematic.name)) {
			toCompile.add(new ImmutableTriple<>(compiledSchematics.get(schematic.name).getLeft(),
					compiledSchematics.get(schematic.name).getMiddle(),
					(1 + compiledSchematics.get(schematic.name).getRight()) % 4));
			// delete the display list associated with the schematic
			GLAllocation.deleteDisplayLists(
					compiledDisplayListId.get(compiledSchematics.get(schematic.name).getLeft()).getLeft());
			compiledSchematics.remove(schematic.name);
		}
	}

	@SubscribeEvent
	public void onRenderTick(RenderWorldLastEvent event) {
		for (Pair<Integer, BlockPos> compiledSchem : compiledDisplayListId.values()) {
			SchematicRenderer.renderCompiledSchematic(compiledSchem.getLeft(), compiledSchem.getRight());
		}
		List<Triple<Schematic, BlockPos, Integer>> remove = Lists.newArrayList();
		for (Triple<Schematic, BlockPos, Integer> schem : toCompile) {
			int id = SchematicRenderer.compileSchematic(schem.getLeft(), schem.getMiddle(), schem.getRight());
			if (id > 0) {
				compiledSchematics.put(schem.getLeft().name, schem);
				compiledDisplayListId.put(schem.getLeft(), new ImmutablePair<>(id, schem.getMiddle()));
				remove.add(schem);
			}
		}
		toCompile.removeAll(remove);
	}
}
