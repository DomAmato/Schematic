package com.dyn.schematics.registry;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.renderer.SchematicRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SchematicRenderingRegistry {
	private static Map<String, Triple<Schematic, BlockPos, Pair<EnumFacing, Integer>>> compiledSchematics = Maps
			.newHashMap();
	private static Map<Schematic, Pair<Integer, BlockPos>> compiledDisplayListId = Maps.newHashMap();
	private static List<Triple<Schematic, BlockPos, Pair<EnumFacing, Integer>>> toCompile = Lists.newArrayList();

	public static void addSchematic(Schematic schematic, BlockPos offset, EnumFacing face, int rotation) {
		if (schematic.getSize() < 100000) {
			if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
				// if we already compiled this schematic with the same offset
				// and rotation dont add it again
				if ((SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getMiddle() != offset)
						|| (SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight()
								.getLeft() != face)
						|| (SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight()
								.getRight() != rotation)) {
					SchematicRenderingRegistry.toCompile
							.add(new ImmutableTriple<>(schematic, offset, new ImmutablePair<>(face, rotation)));
					// delete the display list associated with the schematic
					Minecraft.getMinecraft().addScheduledTask(() -> {
						GLAllocation.deleteDisplayLists(SchematicRenderingRegistry.compiledDisplayListId
								.get(SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getLeft())
								.getLeft());
						SchematicRenderingRegistry.compiledSchematics.remove(schematic.getName());
					});

				}
			} else {
				if (schematic.getSize() > 0) {
					SchematicRenderingRegistry.toCompile
							.add(new ImmutableTriple<>(schematic, offset, new ImmutablePair<>(face, rotation)));
				}
			}
		}
	}

	public static boolean containsCompiledSchematic(Schematic schematic) {
		return SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName());
	}

	public static boolean containsCompiledSchematic(Schematic schematic, BlockPos pos) {
		if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
			return SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getMiddle().equals(pos);
		}
		return false;
	}

	public static boolean containsCompiledSchematic(Schematic schematic, BlockPos pos, EnumFacing face, int rotation) {
		if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
			return SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getMiddle().equals(pos)
					&& (SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight()
							.getLeft() == face)
					&& (SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight()
							.getRight() == rotation);
		}
		return false;
	}

	public static int getSchematicRotation(Schematic schematic) {
		if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
			return SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight().getRight();
		}
		return 0;
	}

	public static void removeSchematic(Schematic schematic) {
		if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				GLAllocation.deleteDisplayLists(SchematicRenderingRegistry.compiledDisplayListId
						.get(SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getLeft())
						.getLeft());
				SchematicRenderingRegistry.compiledDisplayListId
						.remove(SchematicRenderingRegistry.compiledSchematics.remove(schematic.getName()).getLeft());
			});

		}
	}

	public static void rotateSchematic(Schematic schematic) {
		if (SchematicRenderingRegistry.compiledSchematics.containsKey(schematic.getName())) {
			SchematicRenderingRegistry.toCompile.add(new ImmutableTriple<>(
					SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getLeft(),
					SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getMiddle(),
					new ImmutablePair<>(SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight().getLeft(),
					(1 + SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getRight().getRight())
							% 4)));
			// delete the display list associated with the schematic
			Minecraft.getMinecraft().addScheduledTask(() -> {
				GLAllocation.deleteDisplayLists(SchematicRenderingRegistry.compiledDisplayListId
						.get(SchematicRenderingRegistry.compiledSchematics.get(schematic.getName()).getLeft())
						.getLeft());
				SchematicRenderingRegistry.compiledSchematics.remove(schematic.getName());
			});

		}
	}

	@SubscribeEvent
	public void onRenderTick(RenderWorldLastEvent event) {

		renderSchematicSelection();

		for (Pair<Integer, BlockPos> compiledSchem : SchematicRenderingRegistry.compiledDisplayListId.values()) {
			SchematicRenderer.renderCompiledSchematic(compiledSchem.getLeft(), compiledSchem.getRight());
		}
		List<Triple<Schematic, BlockPos, Pair<EnumFacing, Integer>>> remove = Lists.newArrayList();
		for (Triple<Schematic, BlockPos, Pair<EnumFacing, Integer>> schem : SchematicRenderingRegistry.toCompile) {
			int id = SchematicRenderer.compileSchematic(schem.getLeft(), schem.getMiddle(), schem.getRight().getLeft(),
					schem.getRight().getRight());
			if (id > 0) {
				SchematicRenderingRegistry.compiledSchematics.put(schem.getLeft().getName(), schem);
				SchematicRenderingRegistry.compiledDisplayListId.put(schem.getLeft(),
						new ImmutablePair<>(id, schem.getMiddle()));
				remove.add(schem);
			}
		}
		SchematicRenderingRegistry.toCompile.removeAll(remove);
	}

	/**
	 * must be translated to proper point before calling
	 */
	private void renderBox() {
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();

		wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

		// FRONT
		wr.pos(-0.5, -0.5, -0.5).endVertex();
		wr.pos(-0.5, 0.5, -0.5).endVertex();

		wr.pos(-0.5, 0.5, -0.5).endVertex();
		wr.pos(0.5, 0.5, -0.5).endVertex();

		wr.pos(0.5, 0.5, -0.5).endVertex();
		wr.pos(0.5, -0.5, -0.5).endVertex();

		wr.pos(0.5, -0.5, -0.5).endVertex();
		wr.pos(-0.5, -0.5, -0.5).endVertex();

		// BACK
		wr.pos(-0.5, -0.5, 0.5).endVertex();
		wr.pos(-0.5, 0.5, 0.5).endVertex();

		wr.pos(-0.5, 0.5, 0.5).endVertex();
		wr.pos(0.5, 0.5, 0.5).endVertex();

		wr.pos(0.5, 0.5, 0.5).endVertex();
		wr.pos(0.5, -0.5, 0.5).endVertex();

		wr.pos(0.5, -0.5, 0.5).endVertex();
		wr.pos(-0.5, -0.5, 0.5).endVertex();

		// betweens.
		wr.pos(0.5, 0.5, -0.5).endVertex();
		wr.pos(0.5, 0.5, 0.5).endVertex();

		wr.pos(0.5, -0.5, -0.5).endVertex();
		wr.pos(0.5, -0.5, 0.5).endVertex();

		wr.pos(-0.5, -0.5, -0.5).endVertex();
		wr.pos(-0.5, -0.5, 0.5).endVertex();

		wr.pos(-0.5, 0.5, -0.5).endVertex();
		wr.pos(-0.5, 0.5, 0.5).endVertex();

		Tessellator.getInstance().draw();
	}

	private void renderSchematicSelection() {
		EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
		if (player == null) {
			return;
		}

		double renderPosX = TileEntityRendererDispatcher.staticPlayerX;
		double renderPosY = TileEntityRendererDispatcher.staticPlayerY;
		double renderPosZ = TileEntityRendererDispatcher.staticPlayerZ;
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(-renderPosX + 0.5, -renderPosY + 0.5, -renderPosZ + 0.5);

			GlStateManager.disableTexture2D();
			GlStateManager.enableRescaleNormal();
			GlStateManager.disableLighting();
			GL11.glLineWidth(6);

			boolean seeThrough = true;
			while (true) {
				if (seeThrough) {
					GlStateManager.disableDepth();
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				} else {
					GlStateManager.disableBlend();
					GlStateManager.enableDepth();
				}

				// render start
				if (!SchematicMod.startPos.equals(BlockPos.ORIGIN)) {
					GlStateManager.pushMatrix();
					{
						GlStateManager.translate(SchematicMod.startPos.getX(), SchematicMod.startPos.getY(),
								SchematicMod.startPos.getZ());
						GlStateManager.scale(0.96F, 0.96F, 0.96F);
						if (seeThrough) {
							GlStateManager.color(1, 1, 0, .25F);
						} else {
							GlStateManager.color(1, 1, 0);
						}
						renderBox();
					}
					GlStateManager.popMatrix();
				}

				// render end
				if (!SchematicMod.endPos.equals(BlockPos.ORIGIN)) {
					GlStateManager.pushMatrix();
					{
						GlStateManager.translate(SchematicMod.endPos.getX(), SchematicMod.endPos.getY(),
								SchematicMod.endPos.getZ());
						GlStateManager.scale(0.98F, 0.98F, 0.98F);
						if (seeThrough) {
							GlStateManager.color(1, 1, 0, .25F);
						} else {
							GlStateManager.color(1, 1, 0);
						}
						renderBox();
					}
					GlStateManager.popMatrix();
				}

				// render box
				if (!SchematicMod.startPos.equals(BlockPos.ORIGIN) && !SchematicMod.endPos.equals(BlockPos.ORIGIN)) {

					GlStateManager.pushMatrix();
					{
						GlStateManager.translate(
								(float) (SchematicMod.startPos.getX() + SchematicMod.endPos.getX()) / 2,
								(float) (SchematicMod.startPos.getY() + SchematicMod.endPos.getY()) / 2,
								(float) (SchematicMod.startPos.getZ() + SchematicMod.endPos.getZ()) / 2);
						GlStateManager.scale(1 + Math.abs(SchematicMod.startPos.getX() - SchematicMod.endPos.getX()),
								1 + Math.abs(SchematicMod.startPos.getY() - SchematicMod.endPos.getY()),
								1 + Math.abs(SchematicMod.startPos.getZ() - SchematicMod.endPos.getZ()));
						if (seeThrough) {
							GlStateManager.color(1, 1, 1, .25F);
						} else {
							GlStateManager.color(1, 1, 1);
						}
						renderBox();
					}
					GlStateManager.popMatrix();
				}

				if (!seeThrough) {
					break;
				}
				seeThrough = false;
			}
			GlStateManager.enableTexture2D();
		}
		GlStateManager.popMatrix();
	}
}
