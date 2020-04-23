package fr.olympa.zta.hub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.region.Region;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.utils.DynmapLink;

public class HubManager implements Listener {

	private Region region;
	private Location spawnpoint;
	private Location teleportWait;
	private Set<SpawnType> spawnRegions;

	private List<Region> cachedRegions;
	private Region onlySpawnRegion;
	private Random random = new Random();

	public int minDistance = 40;

	public HubManager(Region region, Location spawnpoint, Location teleportWait, List<SpawnType> spawnRegions) {
		this.region = region;
		this.spawnpoint = spawnpoint;
		this.teleportWait = teleportWait;
		this.spawnRegions = new HashSet<>(spawnRegions);
	}

	public void addSpawnRegion(SpawnType region) {
		spawnRegions.add(region);
		cachedRegions = null;
	}

	public void removeSpawnRegion(SpawnType region) {
		spawnRegions.remove(region);
		cachedRegions = null;
	}

	public Set<SpawnType> getSpawnRegions() {
		return spawnRegions;
	}

	public boolean isInHub(Location loc) {
		return region.isIn(loc);
	}

	public void teleport(Player p) {
		p.teleport(spawnpoint);

		DynmapLink.setPlayerVisiblity(p, false);
	}

	public void startRandomTeleport(Player p) {
		p.teleport(teleportWait);
		p.sendTitle("§8Patientez...", "§7Vous allez être téléporté.", 10, 1000, 0);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (cachedRegions == null) {
					cachedRegions = new ArrayList<>();
					spawnRegions.forEach(x -> cachedRegions.addAll(x.getRegions()));
					if (cachedRegions.size() == 1) onlySpawnRegion = cachedRegions.get(0);
				}

				Region region = onlySpawnRegion;
				Location lc = null;
				int minFoundDistance = Integer.MAX_VALUE;
				attempt: for (int i = 0; i < 1000; i++) {
					if (region == null) region = cachedRegions.get(random.nextInt(cachedRegions.size()));
					lc = region.getRandomLocation();
					lc.setY(lc.getWorld().getHighestBlockYAt(lc));
					for (Player p : Bukkit.getOnlinePlayers()) {
						int distance = (int) p.getLocation().distance(lc);
						if (distance > minDistance) {
							if (distance < minFoundDistance) minFoundDistance = distance;
							continue attempt;
						}
					}
					if (MobSpawning.UNSPAWNABLE_ON.contains(lc.getWorld().getBlockAt(lc.getBlockX(), lc.getBlockY() + 1, lc.getBlockZ()).getType())) continue;

					p.teleport(lc); // le joueur est téléporté
					DynmapLink.setPlayerVisiblity(p, true);
					return;
				}
				
				Prefix.DEFAULT_BAD.sendMessage(p, "Une erreur est survenue lors de votre envoi aléatoire.");
				ZTAPermissions.PROBLEM_MONITORING.sendMessage(Prefix.ERROR.toString() + "L'envoi aléatoire du joueur " + p.getName() + " a échoué. Plus petite distance trouvée : " + minFoundDistance);
				p.resetTitle();
				p.teleport(spawnpoint);
			}
		}.runTaskAsynchronously(OlympaZTA.getInstance());
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

		startRandomTeleport(p);
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
