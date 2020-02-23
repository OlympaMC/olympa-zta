package fr.olympa.zta.clans;

import java.util.Arrays;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.NMS;
import fr.olympa.zta.OlympaPlayerZTA;

public class ClansListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		OlympaPlayerZTA oplayer = AccountProvider.get(e.getPlayer());
		String name = e.getPlayer().getName();
		if (!ClansManager.enemiesBukkit.hasEntry(name)) ClansManager.enemiesBukkit.addEntry(name);
		NMS.sendPacket(NMS.addPlayersToTeam(ClansManager.clan, Arrays.asList(e.getPlayer().getName())), e.getPlayer());
		Clan clan = ClansManager.getPlayerClan(oplayer);
		if (clan != null) clan.memberJoin(oplayer);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		OlympaPlayerZTA oplayer = AccountProvider.get(e.getPlayer());
		Clan clan = ClansManager.getPlayerClan(oplayer);
		if (clan != null) clan.memberLeave(oplayer);
	}

}
