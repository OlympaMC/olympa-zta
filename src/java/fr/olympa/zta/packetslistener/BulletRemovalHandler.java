package fr.olympa.zta.packetslistener;

import java.lang.reflect.Field;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_13_R2.PacketPlayOutSpawnEntity;

@Sharable
public class BulletRemovalHandler extends ChannelDuplexHandler{
	
	BulletRemovalHandler(){}
	
	public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception{
		if (msg instanceof PacketPlayOutSpawnEntity) {
			PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) msg;
			Field k = PacketPlayOutSpawnEntity.class.getDeclaredField("k"); // field qui contient le type d'entité
			k.setAccessible(true);
			if (k.getInt(packet) == 61) return; // 61=snowball : return pour ne pas call write (annule le packet)
		}
		super.write(ctx, msg, promise);
	};
	
}
