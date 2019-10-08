package fr.olympa.zta.packetslistener;

import java.lang.reflect.Field;
import java.util.UUID;

import fr.olympa.zta.weapons.guns.bullets.Bullet;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_13_R2.PacketPlayOutSpawnEntity;

public abstract class PacketHandlers{
	
	public static final ChannelDuplexHandler REMOVE_SNOWBALLS = new ChannelDuplexHandler(){
		public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception{
			if (msg instanceof PacketPlayOutSpawnEntity) {
				PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) msg;
				Field b = PacketPlayOutSpawnEntity.class.getDeclaredField("b");
				b.setAccessible(true);
				UUID uuid = (UUID) b.get(packet);
				if (Bullet.toRemove.contains(uuid)) {
					Bullet.toRemove.remove(uuid);
					return;
				}
			}
			super.write(ctx, msg, promise);
		};
	};
	
}
