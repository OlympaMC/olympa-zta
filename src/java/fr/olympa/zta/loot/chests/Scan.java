package fr.olympa.zta.loot.chests;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.markers.AreaMarker;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot.ClanPlotFlag;
import fr.olympa.zta.loot.chests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.utils.map.DynmapLink;

public class Scan { // last working scan : https://gitlab.com/olympa/olympazta/-/commit/9e31ec544f2363357fdb38d0fc5729e79b12bcbe

	private static final double DIVIDE = 6D;
	
	private static final List<Material> containersNoChest = Arrays.asList(Material.TRAPPED_CHEST, Material.BARREL, Material.BREWING_STAND, Material.DISPENSER, Material.DROPPER, Material.FURNACE, Material.HOPPER, Material.SHULKER_BOX);
	
	private final LootChestsManager manager = OlympaZTA.getInstance().lootChestsManager;

	private int chunkProcessed = 0;
	private int processed = 0;
	private int chestsCreated = 0;
	private int chestsAlreadyPresent = 0;
	private int containersEmptied = 0;
	private BukkitTask messages = null;
	private BukkitTask syncTasks = null;
	
	private Lock chestsLock = new ReentrantLock();
	private List<Entry<Location, SpawnType>> chests = new ArrayList<>();
	private Lock containersLock = new ReentrantLock();
	private List<Location> containers = new ArrayList<>();
	
	private Map<Integer, Thread> threads = new HashMap<>();

	private CommandSender sender;

	public void start(CommandSender sender, int minX, int minZ, int maxX, int maxZ, double forward) {
		this.sender = sender;
		
		Prefix.INFO.sendMessage(sender, "Démarrage du scan des blocs %d %d à %d %d, avec un pourcentage d'avancée initial de %f%%.", minX, minZ, maxX, maxZ, forward);

		Random random = new Random();
		
		World world = OlympaZTA.getInstance().mobSpawning.world;
		int minChunkX = minX >> 4;
		int minChunkZ = minZ >> 4;
		int maxChunkX = maxX >> 4;
		int maxChunkZ = maxZ >> 4;
		int xD = (int) Math.ceil((maxChunkX - minChunkX) / DIVIDE);
		int zD = (int) Math.ceil((maxChunkZ - minChunkZ) / DIVIDE);
		int xForward = (int) (xD * forward / 100D);
		int zForward = (int) (zD * forward / 100D);
		int id = 0;
		for (int x = minChunkX; x < maxChunkX; x += xD) {
			for (int z = minChunkZ; z < maxChunkZ; z += zD) {
				startThread(id++, world, x + xForward, z + zForward, x + xD, z + zD);
			}
		}
		
		syncTasks = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
			chestsLock.lock();
			chests.forEach(entry -> {
				Location location = entry.getKey();
				SpawnType spawn = entry.getValue();
				Chest chestBlock = (Chest) location.getBlock().getState();
				//chestBlock.getPersistentDataContainer().remove(LootChestsManager.LOOTCHEST);
				chestBlock.getInventory().clear();
				chestBlock = (Chest) location.getBlock().getState();
				//chestBlock.update();
				LootChest lootChest = manager.getLootChest(chestBlock);
				if (lootChest != null) {
					if (!lootChest.getLocation().equals(location)) lootChest = null; // misplaced chest
				}
				if (lootChest == null) {
					LootChestType type = spawn.getLootChests().pickOne(random);
					try {
						manager.createLootChest(chestBlock.getLocation(), type);
						chestsCreated++;
					}catch (SQLException e) {
						e.printStackTrace();
					}
				}else chestsAlreadyPresent++;
			});
			chests.clear();
			chestsLock.unlock();
			
			containersLock.lock();
			containers.forEach(location -> {
				Container container = (Container) location.getBlock().getState();
				if (container.getInventory() != null) {
					container.getInventory().clear();
					//container.update();
					containersEmptied++;
				}
			});
			containers.clear();
			containersLock.unlock();
			
			if (threads.isEmpty()) {
				Prefix.DEFAULT_GOOD.sendMessage(sender, "Scan terminé ! " + getMessage());
				messages.cancel();
				messages = null;
				syncTasks.cancel();
				syncTasks = null;
			}
		}, 20, 5);
		
		messages = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
			Prefix.INFO.sendMessage(sender, "Scan en cours... " + getMessage());
		}, 100, 600);
	}

	private String getMessage() {
		return "%d chunks traités, %d blocs traités, %d coffres créés, %d coffres déjà présents, %d containers vidés.".formatted(chunkProcessed, processed, chestsCreated, chestsAlreadyPresent, containersEmptied);
	}
	
	private void startThread(int id, World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
		Prefix.INFO.sendMessage(sender, "Démarrage du thread #%d pour le scan des chunks de %d %d à %d %d.", id, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
		Thread thread = new Thread(() -> {
			List<AreaMarker> markers = new ArrayList<>();
			for (int xChunk = minChunkX; xChunk < maxChunkX; xChunk++) {
				for (int zChunk = minChunkZ; zChunk < maxChunkZ; zChunk++) {
					ChunkSnapshot snapshot = world.getChunkAt(xChunk, zChunk).getChunkSnapshot(true, false, false);
					
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							int xBlock = xChunk << 4 | x;
							int zBlock = zChunk << 4 | z;
							SpawnType spawn = SpawnType.getSpawnType(world, xBlock, zBlock);
							if (spawn == null) continue;
							int highestY = snapshot.getHighestBlockYAt(x, z);
							for (int y = 1; y <= highestY; y++) {
								Material block = snapshot.getBlockType(x, y, z);
								Location location = new Location(world, xBlock, y, zBlock);
								processed++;
								if (block == Material.CHEST) {
									if (OlympaCore.getInstance().getRegionManager().getMostImportantFlag(location, ClanPlotFlag.class) != null) continue;
									chestsLock.lock();
									chests.add(new AbstractMap.SimpleEntry<>(location, spawn));
									chestsLock.unlock();
								}else if (block == Material.ENDER_CHEST) {
									OlympaZTA.getInstance().ecManager.addEnderChest(location, true);
								}else if (containersNoChest.contains(block)) {
									if (OlympaCore.getInstance().getRegionManager().getMostImportantFlag(location, ClanPlotFlag.class) != null) continue;
									containersLock.lock();
									containers.add(location);
									containersLock.unlock();
								}
							}
						}
					}
					chunkProcessed++;
					if (DynmapLink.isEnabled()) {
						markers.add(DynmapLink.getInstance().showDebug(id, world, xChunk << 4, zChunk << 4, (xChunk << 4) + 15, (zChunk << 4) + 15, Color.AQUA.asRGB()));
					}
				}
			}
			threads.remove(id);
			Prefix.INFO.sendMessage(sender, "Thread #%d terminé (restants : %d).", id, threads.size());
			markers.forEach(AreaMarker::deleteMarker);
			DynmapLink.ifEnabled(link -> link.showDebug(id, world, minChunkX * 16, minChunkZ * 16, maxChunkX * 16, maxChunkZ * 16, Color.LIME.asRGB()));
		}, "Scan #" + id);
		thread.start();
		threads.put(id, thread);
	}

}
