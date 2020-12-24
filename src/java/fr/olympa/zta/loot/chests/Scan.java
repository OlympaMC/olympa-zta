package fr.olympa.zta.loot.chests;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.dynmap.markers.AreaMarker;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot.ClanPlotFlag;
import fr.olympa.zta.loot.chests.type.LootChestCreator;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.utils.DynmapLink;

public class Scan { // last working scan : https://gitlab.com/olympa/olympazta/-/commit/9e31ec544f2363357fdb38d0fc5729e79b12bcbe

	private static final double DIVIDE = 6D;
	
	private final LootChestsManager manager = OlympaZTA.getInstance().lootChestsManager;

	private int chunkProcessed = 0;
	private int processed = 0;
	private int chestsCreated = 0;
	private int chestsAlreadyPresent = 0;
	private BukkitTask messages = null;
	private BukkitTask syncTasks = null;
	
	private List<Entry<Block, SpawnType>> chests = Collections.synchronizedList(new ArrayList<>());

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
			chests.forEach(entry -> {
				Block block = entry.getKey();
				SpawnType spawn = entry.getValue();
				Chest chestBlock = (Chest) block.getState();
				//chestBlock.getPersistentDataContainer().remove(LootChestsManager.LOOTCHEST);
				chestBlock.getBlockInventory().clear();
				chestBlock.update();
				LootChest lootChest = manager.getLootChest(chestBlock);
				if (lootChest != null) {
					if (!lootChest.getLocation().equals(block.getLocation())) lootChest = null; // misplaced chest
				}
				if (lootChest == null) {
					LootChestCreator creator = spawn.getLootChests().pick(random).get(0);
					try {
						manager.createLootChest(chestBlock.getLocation(), creator.getType());
						chestsCreated++;
					}catch (SQLException e) {
						e.printStackTrace();
					}
				}else chestsAlreadyPresent++;
			});
			chests.clear();
			
			if (threads.isEmpty()) {
				Prefix.DEFAULT_GOOD.sendMessage(sender, "Scan terminé ! %d chunks traités, %d blocs traités, %d coffres créés, %d coffres déjà présents.", chunkProcessed, processed, chestsCreated, chestsAlreadyPresent);
				messages.cancel();
				messages = null;
				syncTasks.cancel();
				syncTasks = null;
			}
		}, 20, 5);
		
		messages = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
			Prefix.INFO.sendMessage(sender, "Scan en cours... %d chunks traités, %d blocs traités, %d coffres créés, %d coffres déjà présents.", chunkProcessed, processed, chestsCreated, chestsAlreadyPresent);
		}, 100, 600);
	}

	private void startThread(int id, World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
		Prefix.INFO.sendMessage(sender, "Démarrage du thread #%d pour le scan des chunks de %d %d à %d %d.", id, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
		Thread thread = new Thread(() -> {
			List<AreaMarker> markers = new ArrayList<>();
			for (int xChunk = minChunkX; xChunk < maxChunkX; xChunk++) {
				for (int zChunk = minChunkZ; zChunk < maxChunkZ; zChunk++) {
					Chunk chunk = world.getChunkAt(xChunk, zChunk);
					chunk.addPluginChunkTicket(OlympaZTA.getInstance());
					while (!chunk.isLoaded()) {
						try {
							Thread.sleep(40);
						}catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
					
					int xChunkTo = xChunk * 16 + 15;
					int zChunkTo = zChunk * 16 + 15;
					for (int x = xChunk * 16; x <= xChunkTo; x++) {
						for (int z = zChunk * 16; z <= zChunkTo; z++) {
							SpawnType spawn = SpawnType.getSpawnType(world, x, z);
							if (spawn == null) continue;
							int highestY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
							for (int y = 1; y <= highestY; y++) {
								Block block = world.getBlockAt(x, y, z);
								if (block.getType() == Material.CHEST) {
									if (OlympaCore.getInstance().getRegionManager().getApplicableRegions(block.getLocation()).stream().anyMatch(region -> region.getFlag(ClanPlotFlag.class) != null)) continue;
									chests.add(new AbstractMap.SimpleEntry<>(block, spawn));
								}else if (block.getType() == Material.ENDER_CHEST) {
									OlympaZTA.getInstance().ecManager.addEnderChest(block.getLocation(), true);
								}
								processed++;
							}
						}
					}
					chunk.removePluginChunkTicket(OlympaZTA.getInstance());
					chunkProcessed++;
					markers.add(DynmapLink.showDebug(id, world, xChunk * 16, zChunk * 16, xChunkTo, zChunkTo, Color.AQUA.asRGB()));
				}
			}
			threads.remove(id);
			Prefix.INFO.sendMessage(sender, "Thread #%d terminé (restants : %d).", id, threads.size());
			markers.forEach(AreaMarker::deleteMarker);
			DynmapLink.showDebug(id, world, minChunkX * 16, minChunkZ * 16, maxChunkX * 16, maxChunkZ * 16, Color.LIME.asRGB());
		}, "Scan #" + id);
		thread.start();
		threads.put(id, thread);
	}

}
