package fr.olympa.zta.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.utils.spigot.TeleportationManager;
import fr.olympa.zta.OlympaZTA;

public class TeleportationManagerZTA extends TeleportationManager {
	
	public TeleportationManagerZTA(Plugin plugin, OlympaSpigotPermission bypassPermission) {
		super(plugin, bypassPermission);
	}
	
	@Override
	public boolean canBypass(Player p, Location to) {
		return p.getWorld().equals(OlympaZTA.getInstance().plotsManager.getWorld()) || super.canBypass(p, to);
	}
	
}
