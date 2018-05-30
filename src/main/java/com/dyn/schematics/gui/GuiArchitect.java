package com.dyn.schematics.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.network.NetworkManager;
import com.dyn.schematics.network.messages.MessageUpdateSchematicNBT;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiArchitect extends GuiContainer {
	public static final int ID = 0;
	private static final ResourceLocation PLANK = new ResourceLocation("textures/blocks/planks_oak.png");
	private static final ResourceLocation SCHEM = new ResourceLocation("schematics",
			"textures/items/schematic_empty.png");
	private static final ResourceLocation DARK_PLANK = new ResourceLocation("textures/blocks/planks_spruce.png");
	private static final ResourceLocation SLOTS = new ResourceLocation("textures/gui/container/horse.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation(
			"textures/gui/resource_packs.png");

	private final ContainerArchitect desk;
	private final InventoryPlayer playerInventory;
	private int index = 0;
	private Schematic schematic;
	private List<String> schem_list;

	public GuiArchitect(EntityPlayer player, World worldIn) {
		super(new ContainerArchitect(player, worldIn));
		playerInventory = player.inventory;

		desk = (ContainerArchitect) inventorySlots;
		schem_list = SchematicRegistry.enumerateSchematics();
		if (!schem_list.isEmpty()) {
			schematic = SchematicRegistry.load(schem_list.get(0));
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

		mc.getTextureManager().bindTexture(GuiArchitect.PLANK);
		this.drawTexturedModalRect((int) (i - (xSize * .25)), j, 0, 0, (int) (xSize * 1.5), ySize);
		if (desk.getSlot(0).getHasStack()) {
			// int shiftVal = (255 << 24); //the alpha value
			// int val = (9489145+shiftVal); //the rgb value as an int
			// Gui.drawRect(i, j+35, i+xSize, j+ySize - 35, val);
			mc.getTextureManager().bindTexture(GuiArchitect.SCHEM);
			drawScaledTexturedRect(i, j - 16, zLevel, xSize, ySize + 16);
			mc.getTextureManager().bindTexture(GuiArchitect.ICON_OVERLAY_LOCATION);
			drawTexturedModalRect((int) (i + (xSize * 1.05)), (j + (ySize / 2)) - 16, 0,
					isInsideArea(mouseX, mouseY, (int) (i + (xSize * 1.05)), (j + (ySize / 2)) - 16, 32, 32) ? 32 : 0,
					32, 32);
			drawTexturedModalRect((int) (i - (xSize * .2)), (j + (ySize / 2)) - 16, 32,
					isInsideArea(mouseX, mouseY, (int) (i - (xSize * .2)), (j + (ySize / 2)) - 16, 32, 32) ? 32 : 0, 32,
					32);
		}
		mc.getTextureManager().bindTexture(GuiArchitect.DARK_PLANK);
		this.drawTexturedModalRect(i - 5, (int) (j + (ySize * .85)), 0, 0, xSize + 10, (int) (ySize * .14));
		mc.getTextureManager().bindTexture(GuiArchitect.SLOTS);
		this.drawTexturedModalRect(i, j + ySize, 0, ySize - 27, 176, 27);
		this.drawTexturedModalRect(i + 189, j + 119, 0, ySize, 18, 18);
		this.drawTexturedModalRect(i - 31, j + 9, 0, ySize, 18, 18);
		this.drawTexturedModalRect(i - 31, j + 119, 0, ySize, 18, 18);

	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if (desk.getSlot(0).getHasStack()) {
			GlStateManager.disableLighting();
			GlStateManager.disableBlend();

			if (schematic != null) {
				int i = 45;
				int j = 30;
				fontRenderer.drawString(TextFormatting.WHITE + "" + TextFormatting.BOLD + schematic.getName(),
						(i * 2) - (fontRenderer.getStringWidth(schematic.getName()) / 2), j, 14737632);
				int counter = 0;
				Map<SimpleItemStack, Integer> materials = schematic.getRequiredMaterials();

				for (Entry<SimpleItemStack, Integer> material : materials.entrySet()) {
					if (counter > 5) {
						fontRenderer.drawString("Etc...", i, j + 15 + (10 * (counter + 1)), 14737632);
						break;
					}
					String renderS = TextFormatting.WHITE + new ItemStack(material.getKey().getItem()).getDisplayName()
							+ TextFormatting.RESET + ": " + TextFormatting.WHITE + material.getValue();
					fontRenderer.drawString(renderS, i, j + 15 + (10 * (counter + 1)), 14737632);
					counter++;
				}
			}

			if (desk.cost > 0) {
				int i = 8453920;
				boolean flag = true;

				if (!desk.getSlot(2).getHasStack()) {
					flag = false;
				} else if (!desk.getSlot(2).canTakeStack(playerInventory.player)) {
					i = 16736352;
				}

				if (flag) {
					int j = -16777216 | ((i & 16579836) >> 2) | (i & -16777216);
					if (fontRenderer.getUnicodeFlag()) {
						Gui.drawRect(-20 - 3, 65, xSize - 7, 77, -16777216);
						Gui.drawRect(-20, 66, xSize - 8, 76, -12895429);
					} else {
						fontRenderer.drawString(" Cost", -37, 35, j);
						fontRenderer.drawString(" Cost", -36, 34, j);
						fontRenderer.drawString(" Cost", -36, 35, j);
						fontRenderer.drawString(desk.cost + " Gold", -37, 45, j);
						fontRenderer.drawString(desk.cost + " Gold", -36, 44, j);
						fontRenderer.drawString(desk.cost + " Gold", -36, 45, j);
					}

					fontRenderer.drawString(" Cost", -37, 34, i);
					fontRenderer.drawString(desk.cost + " Gold", -37, 44, i);
				}
			}
			GlStateManager.enableLighting();
		}
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
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void initGui() {
		super.initGui();

	}

	private boolean isInsideArea(int mouseX, int mouseY, int x, int y, int width, int height) {
		return (mouseX >= x) && (mouseX <= (x + width)) && (mouseY >= y) && (mouseY <= (y + height));
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (desk.getSlot(0).getHasStack()) {
			if (!schem_list.isEmpty()) {
				int i = (width - xSize) / 2;
				int j = (height - ySize) / 2;
				if (isInsideArea(mouseX, mouseY, (int) (i - (xSize * .2)), (j + (ySize / 2)) - 16, 32, 32)) {
					index--;
					if (index < 0) {
						index = Math.max(schem_list.size() - 1, 0);
					}
					schematic = SchematicRegistry.load(schem_list.get(index));
					updateSchematic();
				} else if (isInsideArea(mouseX, mouseY, (int) (i + (xSize * 1.05)), (j + (ySize / 2)) - 16, 32, 32)) {
					index++;
					index %= schem_list.size();
					index = Math.max(index, 0);
					schematic = SchematicRegistry.load(schem_list.get(index));
					updateSchematic();
				}
			}
		}
	}

	private void updateSchematic() {
		String s = schematic.getName();
		Slot slot = desk.getSlot(0);

		if ((slot != null) && slot.getHasStack() && !slot.getStack().hasDisplayName()
				&& s.equals(slot.getStack().getDisplayName())) {
			s = "";
		}
		if (schematic.getSize() < 10000) {
			// a packet can only contain 32kb which any large schematic will easily eclipse
			NBTTagCompound tag = schematic.writeToNBT(new NBTTagCompound());
			desk.updateSchematicContents(s, tag);
			tag.setString("title", s);
			NetworkManager.sendToServer(new MessageUpdateSchematicNBT(tag));
		} else {

		}
	}
}