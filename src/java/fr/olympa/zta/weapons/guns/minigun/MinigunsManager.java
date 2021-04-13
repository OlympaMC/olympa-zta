package fr.olympa.zta.weapons.guns.minigun;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import fr.olympa.api.utils.Point2D;
import fr.olympa.api.utils.observable.Observable.Observer;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class MinigunsManager implements Listener {
	
	private final Map<Integer, Minigun> miniguns = new HashMap<>();
	private int lastID = 0;
	
	private File minigunsFile;
	private YamlConfiguration minigunsYaml;
	
	protected Map<Player, Minigun> inUse = new HashMap<>();
	
	public MinigunsManager(File minigunsFile) throws IOException {
		this.minigunsFile = minigunsFile;
		
		minigunsFile.getParentFile().mkdirs();
		minigunsFile.createNewFile();
		
		Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> {
			minigunsYaml = YamlConfiguration.loadConfiguration(minigunsFile);
			
			for (String key : minigunsYaml.getKeys(false)) {
				int id = Integer.parseInt(key);
				lastID = Math.max(id + 1, lastID);
				Minigun minigun = Minigun.deserialize(minigunsYaml.getConfigurationSection(key).getValues(false));
				addMinigun(minigun, id);
			}
		});
		new MinigunsCommand(this).register();
	}
	
	public Map<Integer, Minigun> getMiniguns() {
		return miniguns;
	}
	
	public Minigun getMinigun(int id) {
		return miniguns.get(id);
	}
	
	public int addMinigun(Minigun minigun) {
		int id = ++lastID;
		addMinigun(minigun, id);
		return id;
	}

	private void addMinigun(Minigun minigun, int id) {
		miniguns.put(id, minigun);
		minigun.id = id;
		Observer updater = updateMinigun(id, minigun);
		minigun.observe("manager_save", updater);
		minigun.updateChunks();
		try {
			updater.changed();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Observer updateMinigun(int id, Minigun minigun) {
		return () -> {
			try {
				minigunsYaml.set(String.valueOf(id), minigun == null ? null : minigun.serialize());
				minigunsYaml.save(minigunsFile);
			}catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	public void unload() {
		HandlerList.unregisterAll(this);
		
		miniguns.values().forEach(Minigun::destroy);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			Minigun minigun = inUse.get(e.getPlayer());
			if (minigun != null) {
				minigun.interact();
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractAtEntityEvent e) {
		Minigun minigun = inUse.get(e.getPlayer());
		if (minigun != null) {
			minigun.interact();
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) {
			for (Minigun minigun : miniguns.values()) {
				if (minigun.isInUse()) continue;
				if (/*minigun.playerPosition.distanceSquared(e.getTo()) < 0.25*/ SpigotUtils.isSameLocation(minigun.playerPosition, e.getTo())) {
					minigun.approach(e.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onLeavePassenger(EntityDismountEvent e) {
		Minigun minigun = inUse.remove(e.getEntity());
		if (minigun != null) minigun.leave(false);
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (miniguns.isEmpty()) return;
		Point2D chunk = new Point2D(e.getChunk());
		for (Minigun minigun : miniguns.values()) {
			if (minigun.chunks.containsKey(chunk)) {
				minigun.chunks.put(chunk, null);
				minigun.updateChunks();
			}
		}
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		if (miniguns.isEmpty()) return;
		Point2D chunk = new Point2D(e.getChunk());
		for (Minigun minigun : miniguns.values()) {
			if (minigun.chunks.containsKey(chunk)) {
				minigun.chunks.put(chunk, e.getChunk());
				minigun.updateChunks();
			}
		}
	}
	
}
