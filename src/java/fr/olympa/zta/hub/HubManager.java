package fr.olympa.zta.hub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.dynmap.DynmapAPI;

import fr.olympa.api.region.Region;
import fr.olympa.zta.OlympaZTA;

public class HubManager implements Listener {

	public Region region;
	private Location spawnpoint;

	private DynmapAPI dynmap = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");

	public HubManager(Region region, Location spawnpoint) {
		this.region = region;
		this.spawnpoint = spawnpoint;
	}

	public void teleport(Player p) {
		p.teleport(spawnpoint);

		dynmap.setPlayerVisiblity(p, false);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(spawnpoint);
		dynmap.setPlayerVisiblity(e.getPlayer(), false);
	}

	@EventHandler
	public void onPortal(EntityPortalEnterEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();

		Location location = OlympaZTA.getInstance().mobSpawning.region.getRandomLocation();
		location.setY(location.getWorld().getHighestBlockYAt(location));
		p.teleport(location);
		dynmap.setPlayerVisiblity(p, true);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		dynmap.setPlayerVisiblity(e.getPlayer(), !region.isIn(e.getTo()));
	}

}
