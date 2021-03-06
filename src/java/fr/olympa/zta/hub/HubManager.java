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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.flags.DamageFlag;
import fr.olympa.api.spigot.region.tracking.flags.FoodFlag;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.utils.map.DynmapLink;
import fr.olympa.zta.weapons.guns.GunFlag;
import net.citizensnpcs.api.CitizensAPI;
import net.md_5.bungee.api.ChatMessageType;

public class HubManager implements Listener {

	private final Region region;
	private final Location spawnpoint;
	private Set<SpawnType> spawnRegions;

	private List<Region> cachedRegions = null;
	private Region onlySpawnRegion;
	private Random random = new Random();

	private Set<Player> inRandomTP = new HashSet<>();

	public int minDistance = 50;
	public int maxHeight = 50;

	public HubManager(Region region, Location spawnpoint, List<SpawnType> spawnRegions) {
		this.region = region;
		this.spawnpoint = spawnpoint;
		this.spawnRegions = new HashSet<>(spawnRegions);

		OlympaCore.getInstance().getRegionManager().registerRegion(region, "hub", EventPriority.NORMAL,
				new DynmapLink.DynmapHideFlag(),
				new FoodFlag(true, true, false),
				new GunFlag(true, false),
				new DamageFlag(true).setMessages("§e§lBienvenue au Spawn !", null, ChatMessageType.ACTION_BAR));
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

	public Location getSpawnpoint() {
		return spawnpoint;
	}

	public boolean isInHub(Location loc) {
		return region.isIn(loc);
	}

	public void teleport(Player p) {
		OlympaZTA.getInstance().teleportationManager.teleport(p, getSpawnpoint(), Prefix.DEFAULT_GOOD.formatMessage("Tu as été téléporté au spawn."));
	}

	public void startRandomTeleport(Player p) {
		if (!inRandomTP.add(p)) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (cachedRegions == null) {
					cachedRegions = new ArrayList<>();
					spawnRegions.forEach(x -> cachedRegions.addAll(x.getRegions()));
					onlySpawnRegion = cachedRegions.size() == 1 ? cachedRegions.get(0) : null;
				}

				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu vas être téléporté sur le champ de bataille. Bon courage.");
				int minFoundDistance = Integer.MAX_VALUE;
				attempt: for (int i = 0; i < 1000; i++) {
					Region region = onlySpawnRegion == null ? cachedRegions.get(random.nextInt(cachedRegions.size())) : onlySpawnRegion;
					Location lc = region.getRandomLocation();
					int y = lc.getWorld().getHighestBlockYAt(lc);
					if (y > maxHeight) continue attempt;
					lc.setY(y);
					for (Player otherPlayer : region.getWorld().getPlayers()) {
						if (p == otherPlayer) continue;
						int distance = (int) otherPlayer.getLocation().distanceSquared(lc);
						if (distance < minDistance * minDistance) {
							if (distance < minFoundDistance) minFoundDistance = distance;
							continue attempt;
						}
					}
					if (MobSpawning.UNSPAWNABLE_ON.contains(lc.getBlock().getType())) continue;

					Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
						p.teleport(lc.add(0.5, 2, 0.5));
						inRandomTP.remove(p);
					}); // le joueur est téléporté de manière synchrone
					return;
				}
				
				Prefix.DEFAULT_BAD.sendMessage(p, "Une erreur est survenue lors de votre envoi aléatoire.");
				ZTAPermissions.PROBLEM_MONITORING.sendMessage(Prefix.ERROR.toString() + "L'envoi aléatoire du joueur " + p.getName() + " a échoué. Plus petite distance trouvée : " + minFoundDistance);
				p.teleport(getSpawnpoint());
				inRandomTP.remove(p);
			}
		}.runTaskAsynchronously(OlympaZTA.getInstance());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(getSpawnpoint());
	}

	@EventHandler
	public void onPortal(EntityPortalEnterEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (CitizensAPI.getNPCRegistry().isNPC(p)) return;

		startRandomTeleport(p);
	}

}
