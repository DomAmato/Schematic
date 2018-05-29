package com.dyn.schematics.gui;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.network.NetworkManager;
import com.dyn.schematics.network.messages.MessageBuildSchematicFromTileEntity;
import com.dyn.schematics.registry.SchematicRenderingRegistry;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiClaimBlock extends GuiContainer {
	public static final int ID = 1;
	private static final ResourceLocation PLANK = new ResourceLocation("textures/blocks/planks_oak.png");
	private static final ResourceLocation SCHEM = new ResourceLocation("schematics",
			"textures/items/schematic_empty.png");
	private static final ResourceLocation SLOTS = new ResourceLocation("textures/gui/container/horse.png");

	/** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
	private float currentScroll;
	private ClaimBlockTileEntity tile;

	public GuiClaimBlock(EntityPlayer player, World worldIn, BlockPos pos) {
		super(new ContainerClaimBlock(player, worldIn, pos));
		tile = (ClaimBlockTileEntity) worldIn.getTileEntity(pos);
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			if (SchematicMod.can_build) {
				if ((Minecraft.getMinecraft().playerController.getCurrentGameType() == GameType.CREATIVE)
						|| !SchematicMod.req_resources) {
					NetworkManager.sendToServer(new MessageBuildSchematicFromTileEntity(tile.getPos(),
							SchematicRenderingRegistry.getSchematicRotation(tile.getSchematic())));
					SchematicRenderingRegistry.removeSchematic(tile.getSchematic());
					mc.player.closeScreen();
				} else {
					Map<SimpleItemStack, Integer> totalMaterials = tile.getInventory().getTotalMaterials();
					for (Entry<Block, Integer> entry : tile.getSchematic().getRequiredMaterials().entrySet()) {
						ItemStack stack = new ItemStack(entry.getKey());
						int amount = entry.getValue();
						Random rand = new Random();
						if (stack.isEmpty()) {
							stack = new ItemStack(
									entry.getKey().getItemDropped(entry.getKey().getDefaultState(), rand, 0));
							amount = amount * entry.getKey().quantityDropped(rand);
						}
						SimpleItemStack key = new SimpleItemStack(stack);
						if (totalMaterials.get(key) != 0) {
							Minecraft.getMinecraft().player.sendMessage(
									new TextComponentString("Not all materials have been supplied, missing: "
											+ totalMaterials.get(key) + " " + stack.getDisplayName()));
							mc.player.closeScreen();
							return;
						}
					}
					NetworkManager.sendToServer(new MessageBuildSchematicFromTileEntity(tile.getPos(),
							SchematicRenderingRegistry.getSchematicRotation(tile.getSchematic())));
					SchematicRenderingRegistry.removeSchematic(tile.getSchematic());
					mc.player.closeScreen();
				}
			} else {
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Building is not enabled"));
			}
		} else {
			SchematicRenderingRegistry.removeSchematic(tile.getSchematic());
		}
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;

		mc.getTextureManager().bindTexture(GuiClaimBlock.PLANK);
		this.drawTexturedModalRect(i - 24, 18, 0, 0, (int) (xSize * 1.75), height - 36);

		mc.getTextureManager().bindTexture(GuiClaimBlock.SCHEM);
		drawScaledTexturedRect(i - 20, j - 50, zLevel, (int) (xSize * 1.65), (int) (ySize * 1.5));

		mc.getTextureManager().bindTexture(GuiClaimBlock.SLOTS);
		for (int i1 = 0; i1 < 9; ++i1) {
			for (int k1 = 0; k1 < 3; ++k1) {
				this.drawTexturedModalRect((i - 83) + (k1 * 18), (j - 1) + (i1 * 18), 0, ySize, 18, 18);
			}
			this.drawTexturedModalRect(i - 105, (j - 1) + (i1 * 18), 0, ySize, 18, 18);
		}

		for (int i2 = 0; i2 < 28; i2++) {
			ItemStack stack = tile.getInventory().getStackInSlot(i2);
			if (stack != ItemStack.EMPTY) {
				mc.getTextureManager().bindTexture(GuiClaimBlock.SLOTS);
				this.drawTexturedModalRect(i + 47 + (54 * (i2 % 4)), j + 35 + ((i2 / 4) * 18), 0, ySize, 18, 18);
				drawString(fontRenderer,
						"(" + tile.getInventory().getTotalMaterials().get(new SimpleItemStack(stack)) + ")",
						i + 67 + (54 * (i2 % 4)), j + 40 + ((i2 / 4) * 18), -1);
			}

		}

		drawString(fontRenderer, "Plot Claimed for Schematic: ", i + 47, j - 5, -1);
		drawString(fontRenderer,
				WordUtils.capitalizeFully(
						tile.getSchematic().getName().substring(0, tile.getSchematic().getName().lastIndexOf("-"))),
				i + 65, j + 7, -1);
		drawString(fontRenderer, "Remaining Materials Required:", i + 47, j + 22, -1);
	}

	private void drawScaledTexturedRect(int x, int y, float zLevel, int width, int height) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x + width, y + height, zLevel).tex(1, 1).endVertex();
		bufferbuilder.pos(x + width, y, zLevel).tex(1, 0).endVertex();
		bufferbuilder.pos(x, y, zLevel).tex(0, 0).endVertex();
		bufferbuilder.pos(x, y + height, zLevel).tex(0, 1).endVertex();
		tessellator.draw();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		if (((ContainerClaimBlock) inventorySlots).canScroll()) {
			int i = Mouse.getEventDWheel();

			if (i != 0) {
				int j = ((tile.getSchematic().getRequiredMaterials().size() + 3) / 4) - 5;

				if (i > 0) {
					i = 1;
				}

				if (i < 0) {
					i = -1;
				}

				currentScroll = (float) (currentScroll - ((double) i / (double) j));
				currentScroll = MathHelper.clamp(currentScroll, 0.0F, 1.0F);
				((ContainerClaimBlock) inventorySlots).scrollTo(currentScroll);
			}
		}
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void initGui() {
		buttonList.add(new GuiButton(0, (int) (width * .75), (int) (height * .84), 50, 20, "Build"));
		// this.buttonList.add(new GuiButton(1, (int) (this.width * .75), (int)
		// (this.height * .8), 20, 20, "X"));
		super.initGui();

	}
}