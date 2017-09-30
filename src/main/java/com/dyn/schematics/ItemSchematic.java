package com.dyn.schematics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.dyn.schematics.reference.Reference;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.registry.SchematicRenderingRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		setRegistryName(Reference.MOD_ID, "schematic");
		setUnlocalizedName("schematic");
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	/**
	 * allows items to add custom lines of information to the mouseover
	 * description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();

			String schemName = nbttagcompound.getString("title");
			int counter = 0;

			Schematic schem = new Schematic(schemName, nbttagcompound);

			Map<Block, Integer> materials = schem.getRequiredMaterials();

			tooltip.add(TextFormatting.DARK_AQUA + schemName + TextFormatting.RESET + " ("
					+ TextFormatting.GRAY + schem.getTotalMaterialCost(materials) + TextFormatting.RESET + ")");
			tooltip.add("");
			for (Entry<Block, Integer> block : materials.entrySet()) {
				if (counter > 5) {
					tooltip.add("Etc...");
					break;
				}
				tooltip.add(TextFormatting.GOLD + block.getKey().getLocalizedName() + TextFormatting.RESET
						+ ": " + TextFormatting.GRAY + block.getValue());
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
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(new ItemStack(this, 1, 0));
		for (String schemName : SchematicRegistry.enumerateSchematics()) {
			Schematic schem = SchematicRegistry.load(schemName);
			if ((schem != null) && (schem.getSize() < 100000)) {
				NBTTagCompound compound = new NBTTagCompound();
				schem.writeToNBT(compound);
				compound.setString("title", schemName);

				ItemStack is = new ItemStack(this);
				is.setTagCompound(compound);

				items.add(is);

			}
		}
	}

	/**
	 * Called before a block is broken. Return true to prevent default block
	 * harvesting.
	 *
	 * Note: In SMP, this is called on both client and server sides!
	 *
	 * @param itemstack
	 *            The current ItemStack
	 * @param pos
	 *            Block's position in world
	 * @param player
	 *            The Player that is wielding the item
	 * @return True to prevent harvesting, false to continue as normal
	 */
	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		if (player.world.isRemote) {
			if (stack.hasTagCompound()) {
				Schematic schem = new Schematic(stack.getDisplayName(), stack.getTagCompound());
				if (SchematicRenderingRegistry.containsCompiledSchematic(schem, pos)) {
					SchematicMod.proxy.openSchematicGui(true, pos, schem);
				}
			} else if (!SchematicMod.startPos.equals(BlockPos.ORIGIN) && !SchematicMod.endPos.equals(BlockPos.ORIGIN)) {
				SchematicMod.proxy.openSchematicGui(false, pos, null);
			}

		}
		return true;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is
	 * pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
		if (worldIn.isRemote) {
			if (playerIn.getHeldItem(handIn).hasTagCompound()) {
				SchematicRenderingRegistry
						.removeSchematic(new Schematic(playerIn.getHeldItem(handIn).getDisplayName(), playerIn.getHeldItem(handIn).getTagCompound()));
			} else {
				SchematicMod.startPos = BlockPos.ORIGIN;
				SchematicMod.endPos = BlockPos.ORIGIN;
			}
		}
        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
 {
		if (worldIn.isRemote) {
			if (player.getHeldItem(hand).hasTagCompound()) {
				Schematic schem = new Schematic(player.getHeldItem(hand).getDisplayName(), player.getHeldItem(hand).getTagCompound());
				if (SchematicRenderingRegistry.containsCompiledSchematic(schem, pos)) {
					SchematicRenderingRegistry.rotateSchematic(schem);
				} else {
					SchematicRenderingRegistry.addSchematic(schem, pos, 0);
				}
				return EnumActionResult.PASS;
			} else {
				if (SchematicMod.startPos != BlockPos.ORIGIN) {
					SchematicMod.endPos = pos;
				} else {
					SchematicMod.startPos = pos;
				}
			}
		}

		return EnumActionResult.PASS;
	}
}
