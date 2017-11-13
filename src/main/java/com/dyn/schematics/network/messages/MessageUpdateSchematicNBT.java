package com.dyn.schematics.network.messages;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.gui.ContainerArchitect;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateSchematicNBT implements IMessage {

	public static class Handler implements IMessageHandler<MessageUpdateSchematicNBT, IMessage> {
		@Override
		public IMessage onMessage(final MessageUpdateSchematicNBT message, final MessageContext ctx) {
			SchematicMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				ContainerArchitect desk = (ContainerArchitect) player.openContainer;
				desk.updateSchematicContents(message.getTag().getString("title"), message.getTag());

			});
			return null;
		}
	}

	private NBTTagCompound tag;

	public MessageUpdateSchematicNBT() {
	}

	public MessageUpdateSchematicNBT(NBTTagCompound tag) {
		this.tag = tag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	public NBTTagCompound getTag() {
		return tag;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}
