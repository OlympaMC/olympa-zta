package fr.olympa.zta.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.utils.TeleportationManager;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;

public class TeleportationManagerZTA extends TeleportationManager {
	
	public TeleportationManagerZTA(Plugin plugin, OlympaSpigotPermission bypassPermission) {
		super(plugin, bypassPermission);
	}
	
	@Override
	public boolean canBypass(Player p) {
		return p.getWorld().equals(OlympaZTA.getInstance().plotsManager.getWorld()) || super.canBypass(p);
	}
	
	@Override
	public boolean canTeleport(Player p) {
		if (OlympaZTA.getInstance().combat.isInCombat(p)) {
			Prefix.BAD.sendMessage(p, "Tu ne peux pas te téléporter quand tu es en combat.");
			return false;
		}
		return super.canTeleport(p);
	}
	
}
