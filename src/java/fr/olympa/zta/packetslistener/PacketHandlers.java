package fr.olympa.zta.packetslistener;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public enum PacketHandlers {
	
	REMOVE_SNOWBALLS(new BulletRemovalHandler()),
	ITEM_DROP(new DropHandler()),
	BED_MESSAGE(new BedMessageHandler())
	;
	
	private ChannelHandler handler;
	private String handlerName;
	
	private PacketHandlers(ChannelHandler handler) {
		this.handler = handler;
		this.handlerName = handler.getClass().getSimpleName();
	}
	
	public void addPlayer(Player p) {
		try {
			ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
			pipeline.addBefore("packet_handler", handlerName, handler);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void removePlayer(Player p) {
		try {
			ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
			if (pipeline.get(handlerName) != null) pipeline.remove(handlerName);
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Player retrievePlayerFromChannel(Channel channel) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (channel.equals(((CraftPlayer) p).getHandle().playerConnection.networkManager.channel)) return p;
		}
		return null;
	}
	
}
