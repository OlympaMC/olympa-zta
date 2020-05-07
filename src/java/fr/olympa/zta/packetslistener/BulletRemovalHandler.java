package fr.olympa.zta.packetslistener;

import java.lang.reflect.Field;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;

@Sharable
public class BulletRemovalHandler extends ChannelDuplexHandler{
	
	BulletRemovalHandler(){}
	
	public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception{
		if (msg instanceof PacketPlayOutSpawnEntity) {
			PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) msg;
			Field k = PacketPlayOutSpawnEntity.class.getDeclaredField("k"); // field qui contient le type d'entité
			k.setAccessible(true);
			if (k.get(packet) == EntityTypes.LLAMA_SPIT) return; // return pour ne pas call write (annule le packet)
		}
		super.write(ctx, msg, promise);
	};
	
}
