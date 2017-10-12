package com.dyn.schematics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dyn.schematics.block.BlockSchematicClaim;
import com.dyn.schematics.block.BlockSchematicClaimStand;
import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.registry.SchematicRegistry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
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
		setCreativeTab(SchematicMod.schemTab);
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();

			String schemName = nbttagcompound.getString("title");
			int counter = 0;

			Schematic schem = new Schematic(schemName, nbttagcompound);

			Map<Block, Integer> materials = schem.getRequiredMaterials();

			tooltip.add(EnumChatFormatting.DARK_AQUA + schemName + EnumChatFormatting.RESET + " ("
					+ EnumChatFormatting.GRAY + schem.getTotalMaterialCost(materials) + EnumChatFormatting.RESET + ")");
			tooltip.add("");
			for (Entry<Block, Integer> block : materials.entrySet()) {
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
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		for (String schemName : SchematicRegistry.enumerateSchematics()) {
			Schematic schem = SchematicRegistry.load(schemName);
			if ((schem != null) && (schem.getSize() < 100000)) {
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
		if (player.worldObj.isRemote) {
			if (!stack.hasTagCompound() && !SchematicMod.startPos.equals(BlockPos.ORIGIN)
					&& !SchematicMod.endPos.equals(BlockPos.ORIGIN)) {
				SchematicMod.proxy.openSchematicGui(false, pos, null);
				return true;
			}

		}
		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed.
	 * Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		if (worldIn.isRemote) {
			if (!stack.hasTagCompound()) {
				SchematicMod.startPos = BlockPos.ORIGIN;
				SchematicMod.endPos = BlockPos.ORIGIN;
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
		if (stack.hasTagCompound()) {
			pos = pos.offset(side);

			if (!playerIn.canPlayerEdit(pos, side, stack)) {
				return false;
			} else if (worldIn.isRemote) {
				return true;
			} else {
				if ((side == EnumFacing.UP) || (side == EnumFacing.DOWN)) {
					MathHelper.floor_double((((playerIn.rotationYaw + 180.0F) * 16.0F) / 360.0F) + 0.5D);
					worldIn.setBlockState(pos,
							SchematicMod.schematicBlockStand.getDefaultState()
									.withProperty(BlockSchematicClaim.FACING,
											playerIn.getHorizontalFacing().getOpposite())
									.withProperty(BlockSchematicClaimStand.CEILING, side == EnumFacing.DOWN),
							3);
				} else {
					worldIn.setBlockState(pos, SchematicMod.schematicBlockWall.getDefaultState()
							.withProperty(BlockSchematicClaim.FACING, side), 3);
				}

				--stack.stackSize;
				TileEntity tileentity = worldIn.getTileEntity(pos);

				if ((tileentity instanceof ClaimBlockTileEntity)
						&& !ItemBlock.setTileEntityNBT(worldIn, playerIn, pos, stack)) {
					BlockPos schem_pos;

					switch (side) {
					case EAST:
						schem_pos = pos;
						break;
					case NORTH:
						schem_pos = pos.south();
						break;
					case SOUTH:
						schem_pos = pos.east();
						break;
					case WEST:
						schem_pos = pos.south().east();
						break;
					default:
						schem_pos = pos;
						break;
					case DOWN:
						schem_pos = pos.up();
						switch (playerIn.getHorizontalFacing()) {
						case EAST:
							schem_pos = pos.up().south().east();
							break;
						case NORTH:
							schem_pos = pos.up().east();
							break;
						case SOUTH:
							schem_pos = pos.up().south();
							break;
						case WEST:
							schem_pos = pos.up();
							break;
						default:
							schem_pos = pos;
							break;

						}
						break;
					case UP:
						switch (playerIn.getHorizontalFacing()) {
						case EAST:
							schem_pos = pos.down().south().east();
							break;
						case NORTH:
							schem_pos = pos.down().east();
							break;
						case SOUTH:
							schem_pos = pos.down().south();
							break;
						case WEST:
							schem_pos = pos.down();
							break;
						default:
							schem_pos = pos;
							break;
						}
						break;

					}
					Schematic schem = new Schematic(stack.getTagCompound().getString("title") + schem_pos.toLong(),
							stack.getTagCompound());
					((ClaimBlockTileEntity) tileentity).setSchematic(schem, schem_pos);
				}

				return true;
			}
		} else if (worldIn.isRemote) {
			if (SchematicMod.startPos != BlockPos.ORIGIN) {
				SchematicMod.endPos = pos;
			} else {
				SchematicMod.startPos = pos;
			}
		}
		return true;

	}
}
