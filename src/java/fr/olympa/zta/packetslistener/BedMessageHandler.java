package fr.olympa.zta.packetslistener;

import java.lang.reflect.Field;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;

@Sharable
public class BedMessageHandler extends ChannelDuplexHandler{
	
	BedMessageHandler(){}
	
	public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception{
		if (msg instanceof PacketPlayOutChat) {
			PacketPlayOutChat packet = (PacketPlayOutChat) msg;
			Field a = PacketPlayOutChat.class.getDeclaredField("a"); // field qui contient le type d'entité
			a.setAccessible(true);
			IChatBaseComponent compo = (IChatBaseComponent) a.get(packet);
			if (compo instanceof ChatMessage) {
				ChatMessage message = (ChatMessage) compo;
				if (message.getKey().startsWith("block.minecraft.bed.")) return;
			}
		}
		super.write(ctx, msg, promise);
	}
	
}
