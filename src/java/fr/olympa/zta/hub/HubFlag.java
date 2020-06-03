package fr.olympa.zta.hub;

import org.bukkit.entity.Player;

import fr.olympa.api.region.tracking.Flag;
import fr.olympa.zta.utils.DynmapLink;
import net.md_5.bungee.api.ChatMessageType;

public class HubFlag extends Flag {

	public HubFlag() {
		super("§e§lBienvenue au Hub !", null, ChatMessageType.ACTION_BAR);
	}

	@Override
	public boolean enters(Player p) {
		super.enters(p);
		DynmapLink.setPlayerVisiblity(p, false);
		return false;
	}

	@Override
	public boolean leaves(Player p) {
		super.leaves(p);
		DynmapLink.setPlayerVisiblity(p, true);
		return false;
	}

}
