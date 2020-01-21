package fr.olympa.zta.mobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.zta.OlympaZTA;

public class MobSpawning {

	public static final List<Material> UNSPAWNABLE_ON = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA, Material.CACTUS);

	public final World world;

	private BukkitTask[] tasks = new BukkitTask[2];
	private boolean enabled = false;
	private Random random = new Random();

	private List<Location> spawnQueue = new ArrayList<>(150);
	private Lock queueLock = new ReentrantLock();
	private Queue<Integer> averageQueueSize = new LinkedList<>();

	public MobSpawning(World world) {
		this.world = world;
	}

	public void start() {
		tasks[0] = new BukkitRunnable() {
			public void run() { // s'effectue toutes les 5 secondes pour calculer les prochains spawns de la tâche 1
				queueLock.lock();
				List<Location> entities = world.getLivingEntities().stream().map(x -> x.getLocation()).collect(Collectors.toList());
				Set<Chunk> activeChunks = getActiveChunks();
				int spawned = 0;
				for (Chunk chunk : activeChunks) {
					/*for (int dx = 0; dx < 8; dx++) {
						int x = dx * 2;
						for (int dz = 0; dz < 8; dz++) {
							int z = dz * 2;
							Block prev = chunk.getBlock(x, 0, z);
							y: for (int y = 1; y < 140; y++) {
								boolean possible = !UNSPAWNABLE_ON.contains(prev.getType());
								prev = chunk.getBlock(x, y, z);
								if (possible && prev.getType() == Material.AIR && chunk.getBlock(x, y + 1, z).getType() == Material.AIR) {
									if (random.nextFloat() < 0.01) { // 1 chance sur 100
										Block block = chunk.getBlock(x, y, z);
										if (block.getLightLevel() < 8) continue;
										for (Location loc : entities) {
											if (loc.distanceSquared(block.getLocation()) < 100) {
												continue y; // distance aux autres entités obligatoirement > à 10 blocs
											}
										}
										Location lc = block.getLocation();
										entities.add(lc);
										spawnQueue.add(lc);
									}
								}
							}
						}
					}*/
					int mobs = random.nextInt(3);
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
								if (block.getLightLevel() < 8 || world.isThundering()) continue;
								for (Location loc : entities) {
									if (loc.distanceSquared(block.getLocation()) < 144) {
										continue y; // distance aux autres entités obligatoirement > à 12 blocs
									}
								}
								Location lc = block.getLocation();
								//entities.add(lc);
								spawnQueue.add(lc);
								spawned++;
								break y;
							}
						}
					}
				}
				System.out.println("spawned: " + spawned);
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
	private Set<Chunk> getActiveChunks() {
		Set<Chunk> chunks = new HashSet<>(100);
		for (Player p : Bukkit.getOnlinePlayers()) {
			Location lc = p.getLocation();
			if (lc.getChunk().getEntities().length > 15) continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++) {
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					chunks.add(world.getChunkAt(x + ax, z + az));
				}
			}
		}
		return chunks;
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

}
