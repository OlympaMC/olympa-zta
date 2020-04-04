package fr.olympa.zta.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;

import fr.olympa.zta.OlympaZTA;

public class DynmapLink {

	private static DynmapAPI api;

	public static void initialize() {
		try {
			api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
		}catch (Exception ex) {}
		OlympaZTA.getInstance().sendMessage("Dynmap integration " + (api == null ? "disabled." : "enabled."));
	}

	public static void setPlayerVisiblity(Player p, boolean visibility) {
		if (api == null) return;
		api.setPlayerVisiblity(p, visibility);
	}

}
