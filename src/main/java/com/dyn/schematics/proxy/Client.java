package com.dyn.schematics.proxy;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.block.BlockSchematicClaim;
import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.network.NetworkManager;
import com.dyn.schematics.network.messages.MessageBuildSchematicFromTileEntity;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.registry.SchematicRenderingRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Client implements Proxy {

	@Override
	public void addScheduledTask(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work as expected because you will be getting a
		// client player even when you are on the server!
		// Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : ctx.getServerHandler().player;
	}

	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		// this causes null pointers in single player...
		return Minecraft.getMinecraft();
	}

	private boolean hasItem(InventoryPlayer inventory, Item itemIn) {
		for (int i = 0; i < inventory.mainInventory.size(); ++i) {
			if ((inventory.mainInventory.get(i) != null) && (inventory.mainInventory.get(i).getItem() == itemIn)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new SchematicRenderingRegistry());
	}

	@Override
	public void openSchematicGui(boolean build, BlockPos pos, Schematic schem) {
		if (build) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiYesNo((result, id) -> {
				TileEntity tileentity = Minecraft.getMinecraft().player.world.getTileEntity(pos);
				if ((tileentity instanceof ClaimBlockTileEntity)) {
					if (result) {
						if (Minecraft.getMinecraft().playerController.getCurrentGameType() == GameType.CREATIVE) {
							NetworkManager.sendToServer(new MessageBuildSchematicFromTileEntity(pos,
									SchematicRenderingRegistry.getSchematicRotation(schem),
									tileentity.getBlockType().getStateFromMeta(tileentity.getBlockMetadata())
											.getValue(BlockSchematicClaim.FACING)));
						} else {
							// check to make sure they have the materials needed
							// to build it
							Map<Block, Integer> materials = ((ClaimBlockTileEntity) tileentity).getSchematic()
									.getRequiredMaterials();

							InventoryPlayer inventory = new InventoryPlayer(null);
							inventory.copyInventory(Minecraft.getMinecraft().player.inventory);

							for (Entry<Block, Integer> material : materials.entrySet()) {
								int total = material.getValue();
								if (!hasItem(inventory, Item.getItemFromBlock(material.getKey()))) {
									Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
											"You do not have the materials necessary to build this schematic, missing: "
													+ material.getKey().getLocalizedName()));
									Minecraft.getMinecraft().displayGuiScreen(null);
									return;
								}
								if (total != inventory.clearMatchingItems(Item.getItemFromBlock(material.getKey()), -1,
										material.getValue(), null)) {
									Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
											"You do not have enough materials to build this schematic, you need: "
													+ material.getValue() + " "
													+ material.getKey().getLocalizedName()));
									Minecraft.getMinecraft().displayGuiScreen(null);
									return;
								}
							}

							NetworkManager.sendToServer(new MessageBuildSchematicFromTileEntity(pos,
									SchematicRenderingRegistry.getSchematicRotation(schem),
									tileentity.getBlockType().getStateFromMeta(tileentity.getBlockMetadata())
											.getValue(BlockSchematicClaim.FACING)));
						}

					}
					((ClaimBlockTileEntity) tileentity).setActive(false);
					Minecraft.getMinecraft().addScheduledTask(() -> {
						SchematicRenderingRegistry.removeSchematic(((ClaimBlockTileEntity) tileentity).getSchematic());
					});
				}
				Minecraft.getMinecraft().displayGuiScreen(null);
			}, "Build Schematic", "Would you like to build this schematic?", 1));
		} else {
			Minecraft.getMinecraft().displayGuiScreen(new GuiYesNo((result, id) -> {
				if (result) {
					Minecraft.getMinecraft().player.sendChatMessage(String.format(
							"/saveschematic " + SchematicMod.startPos.getX() + " " + SchematicMod.startPos.getY() + " "
									+ SchematicMod.startPos.getZ() + " " + SchematicMod.endPos.getX() + " "
									+ SchematicMod.endPos.getY() + " " + SchematicMod.endPos.getZ()));
				}
				SchematicMod.startPos = BlockPos.ORIGIN;
				SchematicMod.endPos = BlockPos.ORIGIN;
				Minecraft.getMinecraft().displayGuiScreen(null);
			}, "Save Schematic", "Would you like to save this schematic?", 1));
		}

	}

	@Override
	public void postInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		File schematicLocs = new File(Minecraft.getMinecraft().mcDataDir, "schematics");

		if (!schematicLocs.exists()) {
			schematicLocs.mkdir();
		}

		SchematicRegistry.addSchematicLocation(schematicLocs);
	}
}