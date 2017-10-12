package com.dyn.schematics.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.rabbit.gui.render.ShaderProgram;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class SchematicRenderer {
	private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("rabbit", null, "shaders/alpha.frag");

	public static int compileSchematic(Schematic schematic, BlockPos offset, EnumFacing face, int schem_rotation) {
		int retListId = -1;
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(offset.getX() - TileEntityRendererDispatcher.staticPlayerX,
				(offset.getY() - TileEntityRendererDispatcher.staticPlayerY) + 0.01,
				offset.getZ() - TileEntityRendererDispatcher.staticPlayerZ);

		if ((OpenGlHelper.shadersSupported)) {
			GL20.glUseProgram(SchematicRenderer.SHADER_ALPHA.getProgram());
			GL20.glUniform1f(GL20.glGetUniformLocation(SchematicRenderer.SHADER_ALPHA.getProgram(), "alpha_multiplier"),
					0.6f);
		}
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		retListId = GLAllocation.generateDisplayLists(2);
		GL11.glNewList(retListId, GL11.GL_COMPILE);
		try {
			switch (face) {
			case EAST:
				GlStateManager.rotate(90 * face.getHorizontalIndex(), 0, 1, 0);
				break;
			case NORTH:
				GlStateManager.rotate(90 * face.getOpposite().getHorizontalIndex(), 0, 1, 0);
				break;
			case SOUTH:
				GlStateManager.rotate(90 * face.getOpposite().getHorizontalIndex(), 0, 1, 0);
				break;
			case WEST:
				GlStateManager.rotate(90 * face.getHorizontalIndex(), 0, 1, 0);
				break;
			default:
				break;
			}
			for (int i = 0; i < schematic.getSize(); ++i) {
				Block b = Block.getBlockById(schematic.getBlockIdAtIndex(i));
				if (b != null) {
					if (b.getRenderType() == 3) {
						IBlockState state = b.getStateFromMeta(schematic.getBlockMetadataAtIndex(i));
						int posX = i % schematic.getWidth();
						int posZ = ((i - posX) / schematic.getWidth()) % schematic.getLength();
						int posY = (((i - posX) / schematic.getWidth()) - posZ) / schematic.getLength();
						BlockPos pos = schematic.rotatePos(posX, posY, posZ, schem_rotation);
						GlStateManager.matrixMode(GL11.GL_MODELVIEW);
						GlStateManager.pushMatrix();
						GlStateManager.pushAttrib();
						GlStateManager.enableRescaleNormal();
						GlStateManager.enableBlend();
						GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
						GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
						Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
						GlStateManager.color(1.0f, 1.0f, 1.0f);
						GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
						state = schematic.rotationState(state, schem_rotation);
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
			SchematicMod.logger.error("Error preview builder block", e);
		} finally {
			GL11.glEndList();
			if (GL11.glGetError() == 0) {
				GlStateManager.shadeModel(GL11.GL_FLAT);
				if ((OpenGlHelper.shadersSupported)) {
					GL20.glUseProgram(0);
				}
				RenderHelper.disableStandardItemLighting();
				// GlStateManager.translate(-1.0f, 0.0f, -1.0f);
				GlStateManager.popMatrix();
				return retListId;
			}
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
		if ((OpenGlHelper.shadersSupported)) {
			GL20.glUseProgram(0);
		}
		RenderHelper.disableStandardItemLighting();
		// GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
		// we should only get here if there was a gl error...
		return -1;
	}

	public static void renderCompiledSchematic(int id, BlockPos offset) {
		GlStateManager.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(offset.getX() - TileEntityRendererDispatcher.staticPlayerX,
				(offset.getY() - TileEntityRendererDispatcher.staticPlayerY) + 0.01,
				offset.getZ() - TileEntityRendererDispatcher.staticPlayerZ);

		if ((OpenGlHelper.shadersSupported)) {
			GL20.glUseProgram(SchematicRenderer.SHADER_ALPHA.getProgram());
			GL20.glUniform1f(GL20.glGetUniformLocation(SchematicRenderer.SHADER_ALPHA.getProgram(), "alpha_multiplier"),
					0.6f);
		}
		GlStateManager.callList(id);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		if ((OpenGlHelper.shadersSupported)) {
			GL20.glUseProgram(0);
		}
		RenderHelper.disableStandardItemLighting();
		// GlStateManager.translate(-1.0f, 0.0f, -1.0f);
		GlStateManager.popMatrix();
	}
}
