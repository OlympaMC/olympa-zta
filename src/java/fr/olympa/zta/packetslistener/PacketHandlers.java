package fr.olympa.zta.packetslistener;

import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;

public abstract class PacketHandlers{
	
	public static final ChannelDuplexHandler REMOVE_SNOWBALLS = new BulletRemovalHandler();
	public static final ChannelDuplexHandler ITEM_DROP = new DropHandler();
	
	public static Player retrievePlayerFromChannel(Channel channel){
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (((CraftPlayer) p).getHandle().playerConnection.networkManager.channel == channel) return p;
		}
		throw new NoSuchElementException("No player found for specified channel.");
	}
	
}
