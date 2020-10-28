package fr.olympa.zta.lootchests;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.plots.ClanPlot.ClanPlotFlag;
import fr.olympa.zta.lootchests.type.LootChestCreator;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;

public class Scan {

	private static final double DIVIDE = 5D;
	
	private final LootChestsManager manager = OlympaZTA.getInstance().lootChestsManager;
	private final Cache<Integer, Chunk> loadedChunks = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).<Integer, Chunk>removalListener(notif -> {
		Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
			notif.getValue().setForceLoaded(false);
			notif.getValue().unload();
		});
	}).build();
	private int chunkLast = 0;

	private int processed = 0;
	private int chestsCreated = 0;
	private int chestsAlreadyPresent = 0;
	private BukkitTask messages = null;

	private Map<Integer, Thread> threads = new HashMap<>();

	private CommandSender sender;

	public void start(CommandSender sender, int minX, int minZ, int maxX, int maxZ, double forward) {
		this.sender = sender;
		
		Prefix.INFO.sendMessage(sender, "Démarrage du scan des blocs %d %d à %d %d, avec un pourcentage d'avancée initial de %f%%.", minX, minZ, maxX, maxZ, forward);

		World world = OlympaZTA.getInstance().mobSpawning.world;
		int xD = (int) Math.ceil((maxX - minX) / DIVIDE);
		int zD = (int) Math.ceil((maxZ - minZ) / DIVIDE);
		int xForward = (int) (xD * forward / 100D);
		int zForward = (int) (zD * forward / 100D);
		int id = 0;
		for (int x = minX; x < maxX; x += xD) {
			for (int z = minZ; z < maxZ; z += zD) {
				startThread(id++, world, x + xForward, z + zForward, x + xD, z + zD);
			}
		}

		messages = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
			Prefix.INFO.sendMessage(sender, "Scan en cours... %d blocs traités, %d coffres créés, %d coffres déjà présents.", processed, chestsCreated, chestsAlreadyPresent);
		}, 100, 600);
	}

	private void startThread(int id, World world, int minX, int minZ, int maxX, int maxZ) {
		Prefix.INFO.sendMessage(sender, "Démarrage du thread #%d pour le scan des blocs de %d %d à %d %d.", id, minX, minZ, maxX, maxZ);
		Random random = new Random();
		Thread thread = new Thread(() -> {
			for (int x = minX; x < maxX; x++) {
				for (int z = minZ; z < maxZ; z++) {
					SpawnType spawn = SpawnType.getSpawnType(world, x, z);
					if (spawn == null) continue;
					Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
					if (!chunk.isForceLoaded()) {
						int chunkID = chunkLast++;
						Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
							chunk.setForceLoaded(true);
							chunk.load();
							loadedChunks.put(chunkID, chunk);
						});
						while (!loadedChunks.asMap().containsKey(chunkID)) {
							try {
								Thread.sleep(100);
							}catch (InterruptedException e) {
								e.printStackTrace();
								return;
							}
						}
					}

					int highestY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
					for (int y = 1; y <= highestY; y++) {
						Block block = world.getBlockAt(x, y, z);
						if (block.getType() == Material.CHEST) {
							if (OlympaCore.getInstance().getRegionManager().getApplicableRegions(block.getLocation()).stream().anyMatch(region -> region.getFlag(ClanPlotFlag.class) != null)) continue;
							Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> {
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
						}else if (block.getType() == Material.ENDER_CHEST) {
							OlympaZTA.getInstance().ecManager.addEnderChest(block.getLocation(), true);
						}
						processed++;
					}
				}
			}
			threads.remove(id);
			Prefix.INFO.sendMessage(sender, "Thread #%d terminé (restants : %d).", id, threads.size());
			if (threads.isEmpty()) {
				Prefix.DEFAULT_GOOD.sendMessage(sender, "Scan terminé ! %d blocs traités, %d coffres créés, %d coffres déjà présents.", processed, chestsCreated, chestsAlreadyPresent);
				messages.cancel();
				messages = null;
				Prefix.INFO.sendMessage(sender, "Déchargement de %d chunks...", loadedChunks.size());
				loadedChunks.invalidateAll();
				Prefix.DEFAULT_GOOD.sendMessage(sender, "Les chunks ont été déchargés avec succès.");
			}
		}, "Scan #" + id);
		thread.start();
		threads.put(id, thread);
	}

}
