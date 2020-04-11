package fr.olympa.zta.mobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.region.ChunkCuboid;
import fr.olympa.zta.OlympaZTA;
import net.minecraft.server.v1_15_R1.Entity;

public class MobSpawning {

	public static final List<Material> UNSPAWNABLE_ON = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA, Material.CACTUS);

	private BukkitTask[] tasks = new BukkitTask[2];
	private boolean enabled = false;
	private Random random = new Random();

	private LinkedList<Location> spawnQueue = new LinkedList<>();
	private Lock queueLock = new ReentrantLock();
	private Queue<Integer> averageQueueSize = new LinkedList<>();

	public World world = Bukkit.getWorlds().get(0);

	public MobSpawning(List<ChunkCuboid> hard, List<ChunkCuboid> medium, List<ChunkCuboid> easy) {
		SpawnType.HARD.regions.addAll(hard); 
		SpawnType.MEDIUM.regions.addAll(medium); 
		SpawnType.EASY.regions.addAll(easy); 
	}

	public void start() {
		tasks[0] = new BukkitRunnable() {
			public void run() { // s'effectue toutes les 5 secondes pour calculer les prochains spawns de la tâche 1
				queueLock.lock();
				List<Location> entities = world.getLivingEntities().stream().map(x -> x.getLocation()).collect(Collectors.toList());
				Map<Chunk, SpawnType> activeChunks = getActiveChunks();
				for (Entry<Chunk, SpawnType> entry : activeChunks.entrySet()) {
					Chunk chunk = entry.getKey();
					SpawnType spawn = entry.getValue();
					int mobs = random.nextInt(spawn.spawnAmount);
					for (int i = 0; i < mobs; i++) {
						int x = random.nextInt(16);
						int y = 1 + random.nextInt(30);
						int z = random.nextInt(16);
						Block prev = chunk.getBlock(x, y, z);
						y: for (; y < 140; y++) {
							boolean possible = !UNSPAWNABLE_ON.contains(prev.getType());
							prev = chunk.getBlock(x, y, z);
							if (possible && prev.getType() == Material.AIR && chunk.getBlock(x, y + 1, z).getType() == Material.AIR) {
								Block block = chunk.getBlock(x, y, z);
								if (block.getLightLevel() > 10 && !world.isThundering()) continue;
								for (Location loc : entities) {
									if (loc.distanceSquared(block.getLocation()) < spawn.minDistanceSquared) {
										continue y; // distance aux autres entités obligatoirement > à 12 blocs
									}
								}
								Location lc = block.getLocation();
								spawnQueue.add(lc);
								break y;
							}
						}
					}
				}
				queueLock.unlock();
			}
		}.runTaskTimerAsynchronously(OlympaZTA.getInstance(), 40L, 102L);

		tasks[1] = new BukkitRunnable() { // s'effectue toutes les 2 secondes et demie pour spawner la moitié des mobs calculés dans la tâche 0
			public void run() {
				averageQueueSize.add(spawnQueue.size());
				if (averageQueueSize.size() > 24) averageQueueSize.remove();

				if (!queueLock.tryLock()) return;
				for (Iterator<Location> iterator = spawnQueue.iterator(); iterator.hasNext();) {
					try {
						Location loc = iterator.next();
						if (loc.getChunk().isLoaded()) Mobs.spawnCommonZombie(loc);
					}catch (Exception ex) {
						ex.printStackTrace();
					}
					iterator.remove();
				}
				queueLock.unlock();
			}
		}.runTaskTimer(OlympaZTA.getInstance(), 50L, 20L);

		enabled = true;
	}

	private final int chunkRadius = 2;
	private final int chunkRadiusDoubled = chunkRadius * 2;

	private Map<Chunk, SpawnType> getActiveChunks() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Chunk> processedChunks = new HashSet<Chunk>(players.size());
		Map<Chunk, SpawnType> chunks = new HashMap<>(players.size() * 8);
		for (Player p : players) {
			Location lc = p.getLocation();
			Chunk centralChunk = lc.getChunk();
			if (!processedChunks.add(centralChunk)) continue;
			if (SpawnType.getSpawnType(centralChunk) == null) continue;
			if (entityCount(centralChunk) > 15) continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++) {
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					Chunk chunk = world.getChunkAt(x + ax, z + az);
					if (chunks.containsKey(chunk)) continue;
					SpawnType type = SpawnType.getSpawnType(chunk);
					if (type != null) chunks.put(chunk, type);
				}
			}
		}
		return chunks;
	}

	private int entityCount(Chunk chunk) {
		int count = 0;
		net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle(); // + opti de passer par les NMS, sinon Bukkit construit une array avec les bukkit entity et c'est lourd
		for (List<Entity> slice : nmsChunk.entitySlices) {
			count += slice.size();
		}
		return count;
	}

	public double getAverageQueueSize() {
		return averageQueueSize.stream().mapToInt(x -> x).average().orElse(0);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void end() {
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i] != null) {
				tasks[i].cancel();
				tasks[i] = null;
			}
		}
		spawnQueue.clear();
		averageQueueSize.clear();

		enabled = false;
	}
	
	enum SpawnType{
		HARD(10, 4), MEDIUM(12, 3), EASY(13, 2);
		
		private List<ChunkCuboid> regions = new ArrayList<>();
		private int minDistanceSquared;
		private int spawnAmount;
		
		private SpawnType(int minDistance, int spawnAmount) {
			this.minDistanceSquared = minDistance ^ 2;
			this.spawnAmount = spawnAmount;
		}

		public boolean isInto(Chunk chunk) {
			for (ChunkCuboid region : regions) {
				if (region.isIn(chunk)) return true;
			}
			return false;
		}

		public static SpawnType getSpawnType(Chunk chunk) {
			for (SpawnType type : values()) {
				if (type.isInto(chunk)) return type;
			}
			return null;
		}
	}

}
