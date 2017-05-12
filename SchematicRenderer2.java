package com.dyn.schematics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.rabbit.gui.render.ShaderProgram;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import noppes.npcs.CustomNpcs;

public class SchematicRenderer2 {
	private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("rabbit", null, "shaders/alpha.frag");
	private static int displayList = -1;
	private static boolean compiled;
	private static Schematic compiledSchematic;
	private static int compiledRotation;
	private static BlockPos compiledOffset;

	public static void drawSelectionBox(BlockPos pos) {
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		AxisAlignedBB bb = new AxisAlignedBB(BlockPos.ORIGIN, pos);
		RenderGlobal.drawOutlinedBoundingBox(bb, 255, 0, 0, 255);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
	}

	public static void renderSchematic(Schematic schematic, BlockPos offset, int rotation) {
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(offset.getX() - TileEntityRendererDispatcher.staticPlayerX,
				(offset.getY() - TileEntityRendererDispatcher.staticPlayerY) + 0.01,
				offset.getZ() - TileEntityRendererDispatcher.staticPlayerZ);
		if (((rotation / 90) % 2) == 0) {
			drawSelectionBox(new BlockPos(schematic.width, schematic.height, schematic.length));
		} else {
			drawSelectionBox(new BlockPos(schematic.length, schematic.height, schematic.width));
		}
		if (schematic.size < 125000) {
			if (compiled && (compiledSchematic == schematic) && (compiledRotation == rotation)
					&& (compiledOffset == offset)) {
				GlStateManager.callList(displayList);
			} else {
				BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				if (displayList >= 0) {
					GLAllocation.deleteDisplayLists(displayList);
				}
				GL11.glNewList(displayList = GLAllocation.generateDisplayLists(1), 4864);
				try {
					for (int i = 0; i < schematic.size; ++i) {
						Block b = Block.getBlockById(schematic.blockArray[i]);
						if (b != null) {
							if (b.getRenderType() == 3) {
								IBlockState state = b.getStateFromMeta(schematic.blockDataArray[i]);
								int posX = i % schematic.width;
								int posZ = ((i - posX) / schematic.width) % schematic.length;
								int posY = (((i - posX) / schematic.width) - posZ) / schematic.length;
								BlockPos pos = schematic.rotatePos(posX, posY, posZ, rotation);
								GlStateManager.pushMatrix();
								GlStateManager.pushAttrib();
								GlStateManager.enableRescaleNormal();
								GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
								Minecraft.getMinecraft().getTextureManager()
										.bindTexture(TextureMap.locationBlocksTexture);
								GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f);
								GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
								state = schematic.rotationState(state, rotation);
								try {
									dispatcher.renderBlockBrightness(state, 1.0f);
									if (GL11.glGetError() != 0) {
										break;
									}
								} catch (Exception e2) {
								} finally {
									GlStateManager.popAttrib();
									GlStateManager.disableRescaleNormal();
									GlStateManager.popMatrix();
								}
							}
						}
					}
				} catch (Exception e) {
					CustomNpcs.logger.error("Error preview builder block", e);
				} finally {
					GL11.glEndList();
					if (GL11.glGetError() == 0) {
						compiled = true;
						compiledSchematic = schematic;
						compiledRotation = rotation;
						compiledOffset = offset;
					}
				}
			}
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
	}

	public static void renderSchematicAlpha(Schematic schematic, BlockPos offset, int rotation) {
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(offset.getX() - TileEntityRendererDispatcher.staticPlayerX,
				(offset.getY() - TileEntityRendererDispatcher.staticPlayerY) + 0.01,
				offset.getZ() - TileEntityRendererDispatcher.staticPlayerZ);
		if ((rotation % 2) == 0) {
			drawSelectionBox(new BlockPos(schematic.width, schematic.height, schematic.length));
		} else {
			drawSelectionBox(new BlockPos(schematic.length, schematic.height, schematic.width));
		}
		if (schematic.size < 125000) {
			if ((OpenGlHelper.shadersSupported)) {
				GL20.glUseProgram(SHADER_ALPHA.getProgram());
				GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), 0.6f);
			}
			if (compiled && (compiledSchematic == schematic) && (compiledRotation == rotation)
					&& (compiledOffset == offset)) {
				GlStateManager.callList(displayList);
			} else {
				BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				if (displayList >= 0) {
					GLAllocation.deleteDisplayLists(displayList);
				}
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GL11.glNewList(displayList = GLAllocation.generateDisplayLists(1), GL11.GL_COMPILE);
				try {
					for (int i = 0; i < schematic.size; ++i) {
						Block b = Block.getBlockById(schematic.blockArray[i]);
						if (b != null) {
							if (b.getRenderType() == 3) {
								IBlockState state = b.getStateFromMeta(schematic.blockDataArray[i]);
								int posX = i % schematic.width;
								int posZ = ((i - posX) / schematic.width) % schematic.length;
								int posY = (((i - posX) / schematic.width) - posZ) / schematic.length;
								BlockPos pos = schematic.rotatePos(posX, posY, posZ, rotation);
								GlStateManager.matrixMode(GL11.GL_MODELVIEW);
								GlStateManager.pushMatrix();
								GlStateManager.pushAttrib();
								GlStateManager.enableRescaleNormal();
								GlStateManager.enableBlend();
								GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1,
										0);
								GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
								Minecraft.getMinecraft().getTextureManager()
										.bindTexture(TextureMap.locationBlocksTexture);
								GlStateManager.color(1.0f, 1.0f, 1.0f);
								GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
								state = schematic.rotationState(state, rotation);
								try {
									dispatcher.renderBlockBrightness(state, 1.0f);
									if (GL11.glGetError() != 0) {
										break;
									}
								} catch (Exception e2) {
								} finally {
									GlStateManager.disableBlend();
									GlStateManager.shadeModel(GL11.GL_FLAT);
									GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
									GlStateManager.popAttrib();
									GlStateManager.disableRescaleNormal();
									GlStateManager.matrixMode(GL11.GL_MODELVIEW);
									GlStateManager.popMatrix();
								}
							}
						}
					}
				} catch (Exception e) {
					CustomNpcs.logger.error("Error preview builder block", e);
				} finally {
					GL11.glEndList();
					if (GL11.glGetError() == 0) {
						compiled = true;
						compiledSchematic = schematic;
						compiledRotation = rotation;
						compiledOffset = offset;
					}
				}
			}
			GlStateManager.shadeModel(GL11.GL_FLAT);
			if ((OpenGlHelper.shadersSupported)) {
				GL20.glUseProgram(0);
			}
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
	}
}
