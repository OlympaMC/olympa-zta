package fr.olympa.zta.hub;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.olympa.api.region.Region;
import fr.olympa.zta.utils.DynmapLink;

public class HubManager implements Listener {

	public Region region;
	private Location spawnpoint;
	private Region spawnRegion;

	public HubManager(Region region, Location spawnpoint, Region spawnRegion) {
		this.region = region;
		this.spawnpoint = spawnpoint;
		this.spawnRegion = spawnRegion;
	}

	public void teleport(Player p) {
		p.teleport(spawnpoint);

		DynmapLink.setPlayerVisiblity(p, false);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(spawnpoint);
		DynmapLink.setPlayerVisiblity(e.getPlayer(), false);
	}

	@EventHandler
	public void onPortal(EntityPortalEnterEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();

		Location location = spawnRegion.getRandomLocation();
		location.setY(location.getWorld().getHighestBlockYAt(location));
		p.teleport(location);
		DynmapLink.setPlayerVisiblity(p, true);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		DynmapLink.setPlayerVisiblity(e.getPlayer(), !region.isIn(e.getTo()));
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (region.isIn(p)) e.setCancelled(true);
		}
	}

}
