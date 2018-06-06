package com.dyn.schematics.network.messages;

import com.dyn.schematics.SchematicMod;
import com.dyn.schematics.gui.ContainerArchitect;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateArchitectDesk implements IMessage {

	public static class Handler implements IMessageHandler<MessageUpdateArchitectDesk, IMessage> {
		@Override
		public IMessage onMessage(final MessageUpdateArchitectDesk message, final MessageContext ctx) {
			SchematicMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				ContainerArchitect desk = (ContainerArchitect) player.openContainer;
				desk.updateSchematicContents(message.getDir());

			});
			return null;
		}
	}

	private boolean direction;

	public MessageUpdateArchitectDesk() {
	}

	public MessageUpdateArchitectDesk(boolean direction) {
		this.direction = direction;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		direction = buf.readBoolean();
	}

	public boolean getDir() {
		return direction;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(direction);
	}

}
