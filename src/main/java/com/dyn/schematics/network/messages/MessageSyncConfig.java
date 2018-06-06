package com.dyn.schematics.network.messages;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.reference.ModConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSyncConfig implements IMessage {

	public static class Handler implements IMessageHandler<MessageSyncConfig, IMessage> {
		@Override
		public IMessage onMessage(final MessageSyncConfig message, final MessageContext ctx) {
			SchematicMod.proxy.addScheduledTask(() -> {
				ModConfig.readConfigFromNBT(message.getTag());
				ModConfig.setUseLocalConfig(false);
			});
			return null;
		}
	}

	private NBTTagCompound tag;

	public MessageSyncConfig() {
		tag = ModConfig.writeConfigToNBT();
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
