package com.dyn.schematics.network.messages;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.block.ClaimBlockTileEntity;
import com.dyn.schematics.reference.ModConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBuildSchematicFromTileEntity implements IMessage {

	public static class Handler implements IMessageHandler<MessageBuildSchematicFromTileEntity, IMessage> {
		@Override
		public IMessage onMessage(final MessageBuildSchematicFromTileEntity message, final MessageContext ctx) {
			if (ModConfig.getConfig().can_build) {
				SchematicMod.proxy.addScheduledTask(() -> {
					World world = ctx.getServerHandler().player.getEntityWorld();
					TileEntity tileentity = world.getTileEntity(message.getPos());
					if ((tileentity instanceof ClaimBlockTileEntity)
							&& (((ClaimBlockTileEntity) tileentity).getSchematic() != null)) {

						((ClaimBlockTileEntity) tileentity).getSchematic().build(world,
								((ClaimBlockTileEntity) tileentity).getSchematicPos(), message.getRotation(),
								world.getBlockState(message.getPos()).getValue(BlockHorizontal.FACING),
								ctx.getServerHandler().player, message.shouldReplaceAir());
						((ClaimBlockTileEntity) tileentity).setActive(false);
						world.setBlockState(message.getPos(), Blocks.AIR.getDefaultState(), 3);
					}
				});
			} else {
				// probably want to notify the user somehow
				ctx.getServerHandler().player
						.sendMessage(new TextComponentString("Building with Schematics is not enabled on this server"));
			}
			return null;
		}
	}

	private BlockPos pos;
	private int rotation;
	private boolean replaceAir;

	public MessageBuildSchematicFromTileEntity() {
	}

	public MessageBuildSchematicFromTileEntity(BlockPos pos, int rotation, boolean replaceAir) {
		this.pos = pos;
		this.rotation = rotation;
		this.replaceAir = replaceAir;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		rotation = buf.readInt();
		replaceAir = buf.readBoolean();
	}

	/**
	 * @return the pos
	 */
	public BlockPos getPos() {
		return pos;
	}

	public int getRotation() {
		return rotation;
	}

	public boolean shouldReplaceAir() {
		return replaceAir;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(rotation);
		buf.writeBoolean(replaceAir);
	}

}
