package fr.olympa.zta.clans;

import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.olympa.api.utils.NMS;

public class ClansListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		String name = e.getPlayer().getName();
		if (!ClansManager.enemiesBukkit.hasEntry(name)) ClansManager.enemiesBukkit.addEntry(name);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, Arrays.asList(e.getPlayer().getName())), e.getPlayer());
	}

}
