package com.dyn.schematics;

import java.util.List;
import java.util.Map.Entry;

import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.registry.SchematicRenderingRegistry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//import noppes.npcs.controllers.SchematicController;

public class ItemSchematic extends Item {

	public static Schematic getSchematic(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();
			String schemName = nbttagcompound.getString("title");
			return new Schematic(schemName, nbttagcompound);
		}
		return null;
	}

	public ItemSchematic() {
		maxStackSize = 1;
		setHasSubtypes(true);
	}

	/**
	 * allows items to add custom lines of information to the mouseover
	 * description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();

			String schemName = nbttagcompound.getString("title");
			tooltip.add(EnumChatFormatting.DARK_AQUA + schemName);
			tooltip.add("");
			int counter = 0;

			Schematic schem = new Schematic(schemName, nbttagcompound);
			for (Entry<Block, Integer> block : schem.getMaterialCosts().entrySet()) {
				if (counter > 5) {
					tooltip.add("Etc...");
					break;
				}
				tooltip.add(EnumChatFormatting.GOLD + block.getKey().getLocalizedName() + EnumChatFormatting.RESET
						+ ": " + EnumChatFormatting.GRAY + block.getValue());
				counter++;
			}
		}
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye
	 * returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		for (String schemName : SchematicRegistry.enumerateSchematics()) {
			Schematic schem = SchematicRegistry.load(schemName);
			if (schem.size < 100000) {
				NBTTagCompound compound = new NBTTagCompound();
				schem.writeToNBT(compound);
				compound.setString("title", schemName);

				ItemStack is = new ItemStack(itemIn);
				is.setTagCompound(compound);

				subItems.add(is);

			}
		}
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is
	 * pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		if (worldIn.isRemote) {
			if (stack.hasTagCompound()) {
				SchematicRenderingRegistry
						.removeSchematic(new Schematic(stack.getDisplayName(), stack.getTagCompound()));
			}
		}
		return stack;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ) {

		if (worldIn.isRemote) {
			if (stack.hasTagCompound()) {
			Schematic schem = new Schematic(stack.getDisplayName(), stack.getTagCompound());
			if (SchematicRenderingRegistry.containsCompiledSchematic(schem, pos)) {
				SchematicRenderingRegistry.rotateSchematic(schem);
			} else {
				SchematicRenderingRegistry.addSchematic(schem, pos, 0);
			}
			return true;
			}
		}

		return true;
	}
}
