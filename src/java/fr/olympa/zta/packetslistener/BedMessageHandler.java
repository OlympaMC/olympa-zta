package fr.olympa.zta.packetslistener;

import java.lang.reflect.Field;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

@Sharable
public class BedMessageHandler extends ChannelDuplexHandler{
	
	private Field aField;
	
	protected BedMessageHandler() {
		try {
			aField = PacketPlayOutChat.class.getDeclaredField("a"); // field qui contient le message
			aField.setAccessible(true);
		}catch (ReflectiveOperationException ex) {
			aField = null;
		}
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception{
		if (aField != null && msg instanceof PacketPlayOutChat) {
			if (aField.get(msg)instanceof ChatMessage message) {
				if (message.getKey().startsWith("block.minecraft.bed.")) return;
			}
		}
		super.write(ctx, msg, promise);
	}
	
}
