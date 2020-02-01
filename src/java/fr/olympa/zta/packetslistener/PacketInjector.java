package fr.olympa.zta.packetslistener;

import java.util.HashSet;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class PacketInjector{
	
	private static HashSet<String> handlers = new HashSet<>();

	public static void addPlayer(Player p, ChannelHandler handler){
		String handlerName = handler.getClass().getSimpleName();
		handlers.add(handlerName);
		try {
			ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
			pipeline.addBefore("packet_handler", handlerName, handler);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void removePlayer(Player p){
		try {
			ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
			for (String handler : handlers) {
				if (pipeline.get(handler) != null) pipeline.remove(handler);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}