package fr.olympa.zta.mobs;

import java.util.AbstractMap;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Color;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.EvictingQueue;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.utils.Point2D;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.chests.type.LootChestPicker;
import fr.olympa.zta.loot.chests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.utils.DynmapLink;
import net.md_5.bungee.api.ChatMessageType;
import net.minecraft.server.v1_16_R3.Entity;

public class MobSpawning implements Runnable {

	public static final List<Material> UNSPAWNABLE_ON = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA, Material.CACTUS, Material.COBWEB, Material.BARRIER);
	private static final String RADAR = "§8§k§lgdn§r§7";

	private Thread calculationThread;
	private BukkitTask spawnTask;
	public World world = Bukkit.getWorlds().get(0);

	private boolean enabled = false;
	private Random random = new Random();

	private Lock queueLock = new ReentrantLock();
	private LinkedList<Entry<Location, Zombies>> spawnQueue = new LinkedList<>();
	
	private EvictingQueue<Integer> queueSize = EvictingQueue.create(24);
	private EvictingQueue<Long> computeTimes = EvictingQueue.create(12);

	private Set<Region> safeRegions = new HashSet<>();

	public int seaLevel;

	private final int chunkRadius = 2;
	private final int chunkRadiusDoubled = chunkRadius * 2;
	public int criticalEntitiesPerChunk = 13;
	public int maxEntities = 3000;
	
	public long timeActiveChunks;
	public int lastActiveChunks;
	public int lastSpawnedMobs;

	public MobSpawning(int seaLevel, ConfigurationSection spawnRegions, ConfigurationSection safeRegions) {
		this.seaLevel = seaLevel;
		for (String type : spawnRegions.getKeys(false)) {
			try {
				SpawnType.valueOf(type).addRegions((List<Region>) spawnRegions.getList(type));
			}catch (IllegalArgumentException ex) {
				OlympaZTA.getInstance().getLogger().warning("Type de spawn invalide: " + type);
			}
		}

		for (String id : safeRegions.getKeys(false)) {
			addSafeZone(safeRegions.getSerializable(id + ".region", Region.class), id, safeRegions.getString(id + ".title"));
		}
	}
	
	@Override
	public void run() {
		while (true) {
			long time = System.currentTimeMillis();
			queueLock.lock();
			try {
				List<Location> entities = world.getLivingEntities()./*getPlayers().*/stream().map(LivingEntity::getLocation).collect(Collectors.toList());
				if (entities.size() > maxEntities) return;
				long time2 = System.currentTimeMillis();
				Map<ChunkSnapshot, SpawnType> activeChunks = getActiveChunks();
				timeActiveChunks = System.currentTimeMillis() - time2;
				lastActiveChunks = activeChunks.size();
				for (Entry<ChunkSnapshot, SpawnType> entry : activeChunks.entrySet()) { // itère dans tous les chunks actifs
					ChunkSnapshot chunk = entry.getKey();
					SpawnType spawn = entry.getValue();
					int attempts = 0;
					int mobs = /*random.nextInt(spawn.spawnAmount + 1)*/ 1;
					mobs: for (int i = 0; i < mobs; i++) { // boucle pour faire spawner un nombre de mobs aléatoires
						if (++attempts == 5) break;
						int x = random.nextInt(16);
						int z = random.nextInt(16); // random position dans le chunk
						if (spawn == SpawnType.NONE) { // none = chunk océan, tenter de faire spawn un noyé
							Material block = chunk.getBlockType(x, seaLevel, z);
							if (block == Material.WATER) { // si le bloc au niveau de l'océan est de l'eau, spawner
								Location location = new Location(world, chunk.getX() << 4 | x, seaLevel, chunk.getZ() << 4 | z);
								for (Location entityLocation : entities) {
									if (entityLocation.distanceSquared(location) < spawn.minDistanceSquared) continue mobs; // trop proche d'entité = abandon
								}
								spawnQueue.add(new AbstractMap.SimpleEntry<>(location, Zombies.DROWNED));
							}
						}else {
							int highestY = chunk.getHighestBlockYAt(x, z);
							int y = random.nextInt(Math.min(highestY - 1, 40)); // à partir de quelle hauteur ça va tenter de faire spawn
							Material prev = chunk.getBlockType(x, y, z);
							y: for (; y < highestY; y++) { // loop depuis l'hauteur aléatoire jusqu'à 140 (pas de spawn au dessus)
								boolean possible = !UNSPAWNABLE_ON.contains(prev);
								prev = chunk.getBlockType(x, y, z);
								if (possible && prev == Material.AIR && chunk.getBlockType(x, y + 1, z) == Material.AIR) { // si bloc possible en dessous ET air au bloc ET air au-dessus = good
									if (chunk.getBlockSkyLight(x, y, z) <= 5 || chunk.getBlockEmittedLight(x, y, z) > 10) {
										if (random.nextBoolean()) continue; // au fond d'un immeuble pas éclairé : pas intéressant
										mobs++;
										continue mobs;
									}
									Location location = new Location(world, chunk.getX() << 4 | x, y, chunk.getZ() << 4 | z);
									SpawningFlag flag = OlympaCore.getInstance().getRegionManager().getMostImportantFlag(location, SpawningFlag.class);
									if (flag == null || flag.type == null) continue;
									if (OlympaZTA.getInstance().clanPlotsManager.getPlot(location) != null) continue; // si on est dans une parcelle de clan pas de spawn
									for (Location loc : entities) {
										if (loc.distanceSquared(location) < spawn.minDistanceSquared) continue y; // trop près d'autre entité
									}
									for (int j = 0; j < spawn.spawnAmount; j++) spawnQueue.add(new AbstractMap.SimpleEntry<>(location, (spawn.explosiveProb != 0) && random.nextDouble() < spawn.explosiveProb ? Zombies.TNT : Zombies.COMMON));
									continue mobs;
								}
							}
						}
					}
				}
			}finally {
				queueLock.unlock();
				long elapsed = System.currentTimeMillis() - time;
				computeTimes.add(elapsed);
				if (!enabled) break;
				try {
					if (elapsed < 5000) Thread.sleep(5000 - elapsed);
				}catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
		calculationThread = null;
	}
	
	public boolean start() {
		if (calculationThread != null) {
			calculationThread.interrupt();
			return false;
		}
		enabled = true;
		
		calculationThread = new Thread(this, "Mob spawning");
		calculationThread.start();
		
		spawnTask = new BukkitRunnable() { // s'effectue toutes les 2 secondes et demie pour spawner la moitié des mobs calculés dans la tâche 0
			public void run() {
				queueSize.add(spawnQueue.size());

				if (!queueLock.tryLock()) return;
				try {
					lastSpawnedMobs = 0;
					int i = spawnQueue.size() / 2;
					for (Iterator<Entry<Location, Zombies>> iterator = spawnQueue.iterator(); iterator.hasNext();) {
						try {
							Entry<Location, Zombies> loc = iterator.next();
							if (loc.getKey().getChunk().isLoaded()) {
								Mobs.spawnCommonZombie(loc.getValue(), loc.getKey());
								lastSpawnedMobs++;
							}
						}catch (Exception ex) {
							ex.printStackTrace();
						}
						iterator.remove();
						if (--i == 0) return;
					}
				}finally {
					queueLock.unlock();
				}
			}
		}.runTaskTimer(OlympaZTA.getInstance(), 20L, 50L);
		return true;
	}

	private Map<ChunkSnapshot, SpawnType> getActiveChunks() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Chunk> processedChunks = new HashSet<Chunk>(players.size() + 1, 1);
		List<Point2D> points = new ArrayList<>(players.size() * 8);
		Map<ChunkSnapshot, SpawnType> chunks = new HashMap<>(players.size() * 8, 1);
		for (Player p : players) {
			Location lc = p.getLocation();
			Chunk centralChunk = lc.getChunk();
			if (!processedChunks.add(centralChunk)) continue; // chunk déjà calculé
			if (SpawnType.getSpawnType(centralChunk) == null) continue;
			if (entityCount(centralChunk) > criticalEntitiesPerChunk) continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++) {
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					Chunk chunk = world.getChunkAt(x + ax, z + az);
					if (!chunk.isLoaded()) continue;
					ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
					Point2D point = new Point2D(chunk);
					if (points.contains(point)) continue;
					SpawnType type;
					if (snapshot.getBlockType(0, world.getHighestBlockYAt(chunk.getX() << 4, chunk.getZ() << 4, HeightMap.WORLD_SURFACE), 0) == Material.WATER) {
						type = SpawnType.NONE;
					}else type = SpawnType.getSpawnType(chunk);
					if (type != null) {
						if (entityCount(chunk) > type.maxEntitiesPerChunk) continue;
						if (isInSafeZone(chunk)) continue;
						chunks.put(snapshot, type);
						points.add(point);
					}
				}
			}
		}
		return chunks;
	}

	private int entityCount(Chunk chunk) {
		int count = 0;
		net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle(); // + opti de passer par les NMS, sinon Bukkit construit une array avec les bukkit entity et c'est lourd
		for (List<Entity> slice : nmsChunk.entitySlices) {
			count += slice.size();
		}
		return count;
	}

	public void addSafeZone(Region region, String id, String title) {
		OlympaCore.getInstance().getRegionManager().registerRegion(region, id, EventPriority.HIGH, new Flag().setMessages(RADAR + " vous entrez dans une " + title + "§r " + RADAR, RADAR + " vous sortez d'une " + title + "§r " + RADAR, ChatMessageType.ACTION_BAR), new SpawnType.SpawningFlag(null));
		safeRegions.add(region);
		DynmapLink.showSafeArea(region, "z" + id, title);
	}

	public boolean isInSafeZone(Chunk chunk) {
		for (Region safeRegion : safeRegions) {
			if (safeRegion.isIn(chunk.getWorld(), chunk.getX() * 16, safeRegion.getMin().getBlockY(), chunk.getZ() * 16)) return true;
		}
		return false;
	}

	public double getAverageQueueSize() {
		return queueSize.stream().mapToInt(Integer::intValue).average().orElse(0);
	}
	
	public Collection<Long> getLastComputeTimes() {
		return computeTimes;
	}

	public String getEntityCount() {
		int players = 0, zombies = 0, drowned = 0, others = 0;
		for (LivingEntity entity : world.getLivingEntities()) {
			if (entity instanceof Drowned) {
				drowned++;
			}else if (entity instanceof Zombie) {
				zombies++;
			}else if (entity instanceof Player) {
				players++;
			}else others++;
		}
		return String.format("%d joueurs %d zombies %d noyés %d autres", players, zombies, drowned, others);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void end() {
		enabled = false;
		
		spawnTask.cancel();

		spawnQueue.clear();
		queueSize.clear();
		computeTimes.clear();
	}
	
	public enum SpawnType {
		NONE(10, 1, 6, 0, "§cerreur", null, null, null, null, null),
		HARD(10, 2, 6, 0.1, "§c§lzone rouge", Color.RED, "621100", "Zone rouge", "Cette zone présente une forte présence en infectés.", new LootChestPicker().add(LootChestType.CIVIL, 0.5).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.4)),
		MEDIUM(12, 2, 5, 0.08, "§6§lzone à risques", Color.ORANGE, "984C00", "Zone à risques", "La contamination est plutôt importante dans cette zone.", new LootChestPicker().add(LootChestType.CIVIL, 0.7).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.2)),
		EASY(15, 1, 4, 0.012, "§d§lzone modérée", Color.YELLOW, "8B7700", "Zone modérée", "Humains et zombies cohabitent, restez sur vos gardes.", new LootChestPicker().add(LootChestType.CIVIL, 0.8).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.1)),
		SAFE(21, 1, 2, 0.008, "§a§lzone sécurisée", Color.LIME, "668B00", "Zone sécurisée", "C'est un lieu sûr, vous pourrez croiser occasionnellement un infecté.", new LootChestPicker().add(LootChestType.CIVIL, 0.8).add(LootChestType.CONTRABAND, 0.2));
		
		private static Map<Chunk, SpawnType> chunks = new HashMap<>();
		
		private int minDistanceSquared;
		private int spawnAmount;
		private int maxEntitiesPerChunk;
		private double explosiveProb;

		public final Color color;
		public final String htmlColor;
		public final String name;
		public final String description;
		public final String title;

		private final LootChestPicker lootchests;

		private List<Region> regions = new ArrayList<>();
		private Flag flag;

		private SpawnType(int minDistance, int spawnAmount, int maxEntitiesPerChunk, double explosiveProb, String title, Color color, String htmlColor, String name, String description, LootChestPicker lootchests) {
			this.minDistanceSquared = minDistance * minDistance;
			this.spawnAmount = spawnAmount;
			this.maxEntitiesPerChunk = maxEntitiesPerChunk;
			this.explosiveProb = explosiveProb;
			this.color = color;
			this.htmlColor = htmlColor;
			this.name = name;
			this.description = description;
			this.title = title;
			this.lootchests = lootchests;

			if (name != null) flag = new SpawningFlag(this);
		}

		public LootChestPicker getLootChests() {
			return lootchests;
		}

		public boolean isInto(World world, int x, int z) {
			for (Region region : regions) {
				if (region.isIn(world, x, region.getMin().getBlockY(), z)) return true;
			}
			return false;
		}

		public void addRegions(Collection<Region> regions) {
			for (Region region : regions) {
				OlympaCore.getInstance().getRegionManager().registerRegion(region, name() + this.regions.size(), EventPriority.LOW, flag);
				this.regions.add(region);
				DynmapLink.showMobArea(region, this);
			}
		}

		public List<Region> getRegions() {
			return regions;
		}

		public static SpawnType getSpawnType(World world, int x, int z) {
			for (SpawnType type : values()) {
				if (type.isInto(world, x, z)) return type;
			}
			return null;
		}
		
		public static SpawnType getSpawnType(Chunk chunk) {
			if (chunks.containsKey(chunk)) return chunks.get(chunk);
			SpawnType type = getSpawnType(chunk.getWorld(), chunk.getX() * 16, chunk.getZ() * 16);
			chunks.put(chunk, type);
			return type;
		}

		public static class SpawningFlag extends Flag {
			public final SpawnType type;

			public SpawningFlag(SpawnType type) {
				this.type = type;
				if (type != null) super.setMessages(RADAR + " vous entrez dans une " + type.title + "§r " + RADAR, null, ChatMessageType.ACTION_BAR);
			}
		}
	}

}
