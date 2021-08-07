package fr.olympa.zta.packetslistener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;

public enum PacketHandlers {
	
	REMOVE_SNOWBALLS(new BulletRemovalHandler()),
	ITEM_DROP(new DropHandler()),
	BED_MESSAGE(new BedMessageHandler())
	;
	
	public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("player" + UUID.randomUUID().toString().substring(0, 6));
	
	private ChannelHandler handler;
	private String handlerName;
	
	private PacketHandlers(ChannelHandler handler) {
		this.handler = handler;
		this.handlerName = handler.getClass().getSimpleName();
	}
	
	public void addPlayer(Player p) {
		try {
			Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
			channel.attr(PLAYER_KEY).set(p);
			channel.pipeline().addBefore("packet_handler", handlerName, handler);
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
