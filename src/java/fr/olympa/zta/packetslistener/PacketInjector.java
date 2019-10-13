package fr.olympa.zta.packetslistener;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class PacketInjector{
	
	public static void addPlayer(Player p, ChannelHandler handler){
		try {
			ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
			if (pipeline.get("PacketInjector") != null) pipeline.remove("PacketInjector");
			pipeline.addBefore("packet_handler", handler.getClass().getSimpleName(), handler);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void removePlayer(Player p){
		try {
			Channel ch = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
			if (ch.pipeline().get("PacketInjector") != null) {
				ch.pipeline().remove("PacketInjector");
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}