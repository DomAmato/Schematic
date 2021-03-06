package com.dyn.schematics.item;

import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.dyn.schematics.Schematic;
import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.block.BlockSchematicClaimStand;
import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.reference.ModConfig;
import com.dyn.schematics.reference.Reference;
import com.dyn.schematics.registry.SchematicRegistry;
import com.dyn.schematics.utils.SimpleItemStack;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
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
		setUnlocalizedName("schematic");
		setRegistryName(Reference.MOD_ID, "schematic");
		setCreativeTab(SchematicMod.schemTab);
	}

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();
			int counter = 0;
			for (NBTBase tag : nbttagcompound.getTagList("com_mat", Constants.NBT.TAG_COMPOUND)) {
				if (counter > 4) {
					tooltip.add("Etc...");
					break;
				}

				tooltip.add(TextFormatting.GOLD + ((NBTTagCompound) tag).getString("name") + TextFormatting.RESET + ": "
						+ TextFormatting.GRAY + ((NBTTagCompound) tag).getInteger("total"));
				counter++;
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();

			String schemName = nbttagcompound.getString("title");

			int cost = nbttagcompound.getInteger("cost");

			return schemName + TextFormatting.RESET + " ("
					+ (cost <= 500 ? TextFormatting.DARK_GREEN
							: cost <= 1500 ? TextFormatting.YELLOW : TextFormatting.RED)
					+ cost + TextFormatting.RESET + ")";
		}
		return super.getItemStackDisplayName(stack);
	}

	/**
	 * This used to be 'display damage' but its really just 'aux' data in the
	 * ItemStack, usually shares the same variable as damage.
	 *
	 * @param stack
	 * @return
	 */
	@Override
	public int getMetadata(ItemStack stack) {
		return (stack.hasTagCompound() ? 1 : 0);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, 0));
			for (String schemName : SchematicRegistry.enumerateSchematics()) {
				Schematic schem = SchematicRegistry.load(schemName);
				if ((schem != null) && (schem.getSize() < ModConfig.getConfig().max_size)
						&& (schem.getTotalMaterialCost() > 0)) {
					NBTTagCompound compound = new NBTTagCompound();
					schem.writeToNBT(compound);
					compound.setString("title", schemName);
					compound.setInteger("cost", schem.getTotalMaterialCost());
					NBTTagList materials = new NBTTagList();
					int counter = 0;
					for (Entry<SimpleItemStack, Integer> material : schem.getRequiredMaterials().entrySet()) {
						if (counter > 5) {
							break;
						}
						NBTTagCompound mat_tag = new NBTTagCompound();
						mat_tag.setString("name", material.getKey().getVanillStack().getDisplayName());
						mat_tag.setInteger("total", material.getValue());
						materials.appendTag(mat_tag);
						counter++;
					}
					compound.setTag("com_mat", materials);
					ItemStack is = new ItemStack(this);
					is.setTagCompound(compound);

					items.add(is);

				}
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + getMetadata(stack);
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed.
	 * Args: itemStack, world, entityPlayer
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote) {
			if (!playerIn.getHeldItem(handIn).hasTagCompound()) {
				SchematicMod.startPos = BlockPos.ORIGIN;
				SchematicMod.endPos = BlockPos.ORIGIN;
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		if (stack.hasTagCompound()) {
			pos = pos.offset(side);

			if (!playerIn.canPlayerEdit(pos, side, stack)) {
				return EnumActionResult.FAIL;
			} else if (worldIn.isRemote) {
				return EnumActionResult.SUCCESS;
			} else {
				if ((side == EnumFacing.UP) || (side == EnumFacing.DOWN)) {
					MathHelper.floor((((playerIn.rotationYaw + 180.0F) * 16.0F) / 360.0F) + 0.5D);
					worldIn.setBlockState(pos,
							SchematicMod.schematicBlockStand.getDefaultState()
									.withProperty(BlockHorizontal.FACING, playerIn.getHorizontalFacing().getOpposite())
									.withProperty(BlockSchematicClaimStand.CEILING, side == EnumFacing.DOWN),
							3);
				} else {
					worldIn.setBlockState(pos, SchematicMod.schematicBlockWall.getDefaultState()
							.withProperty(BlockHorizontal.FACING, side), 3);
				}

				stack.shrink(1);
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
					((ClaimBlockTileEntity) tileentity).setPlacer(playerIn.getUniqueID());
					((ClaimBlockTileEntity) tileentity).markDirty();
				}

				return EnumActionResult.SUCCESS;
			}
		} else if (worldIn.isRemote) {
			if (playerIn.isSneaking() && !SchematicMod.startPos.equals(BlockPos.ORIGIN)
					&& !SchematicMod.endPos.equals(BlockPos.ORIGIN)) {
				SchematicMod.proxy.openSchematicGui();
			} else {
				if (SchematicMod.startPos != BlockPos.ORIGIN) {
					SchematicMod.endPos = pos;
				} else {
					SchematicMod.startPos = pos;
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
}
