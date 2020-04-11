package fr.olympa.zta.clans;

import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.utils.NMS;
import fr.olympa.zta.OlympaPlayerZTA;

public class ClansListener implements Listener {

	@EventHandler (priority = EventPriority.HIGH)
	public void onJoin(OlympaPlayerLoadEvent e) {
		OlympaPlayerZTA oplayer = e.getOlympaPlayer();
		String name = e.getPlayer().getName();
		if (!ClansManager.enemiesBukkit.hasEntry(name)) ClansManager.enemiesBukkit.addEntry(name);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, Arrays.asList(e.getPlayer().getName())), e.getPlayer());
		Clan clan = oplayer.getClan();
		if (clan != null) clan.memberJoin(oplayer);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		OlympaPlayerZTA oplayer = OlympaPlayerZTA.get(e.getPlayer());
		Clan clan = oplayer.getClan();
		if (clan != null) clan.memberLeave(oplayer);
	}

}
