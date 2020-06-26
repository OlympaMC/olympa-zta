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
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.type.LootChestPicker;
import fr.olympa.zta.lootchests.type.LootChestType;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.utils.DynmapLink;
import net.md_5.bungee.api.ChatMessageType;
import net.minecraft.server.v1_15_R1.Entity;

public class MobSpawning {

	public static final List<Material> UNSPAWNABLE_ON = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA, Material.CACTUS, Material.COBWEB, Material.BARRIER);
	private static final String RADAR = "§8§k§lgdn§r§7";

	private BukkitTask[] tasks = new BukkitTask[2]; // 0: calculation, 1: spawn
	public World world = Bukkit.getWorlds().get(0);

	private boolean enabled = false;
	private Random random = new Random();

	private Lock queueLock = new ReentrantLock();
	private LinkedList<Entry<Location, Zombies>> spawnQueue = new LinkedList<>();
	private Queue<Integer> averageQueueSize = new LinkedList<>();

	private Set<Region> safeRegions = new HashSet<>();

	public int seaLevel;

	private final int chunkRadius = 2;
	private final int chunkRadiusDoubled = chunkRadius * 2;
	public int criticalEntitiesPerChunk = 20;
	public int maxEntities = 3000;

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

	public void start() {
		tasks[0] = new BukkitRunnable() {
			public void run() { // s'effectue toutes les 5 secondes pour calculer les prochains spawns de la tâche 1
				queueLock.lock();
				try {
					List<Location> entities = world.getLivingEntities().stream().map(LivingEntity::getLocation).collect(Collectors.toList());
					if (entities.size() > maxEntities) return;
					Map<Chunk, SpawnType> activeChunks = getActiveChunks();
					for (Entry<Chunk, SpawnType> entry : activeChunks.entrySet()) {
						Chunk chunk = entry.getKey();
						SpawnType spawn = entry.getValue();
						int mobs = random.nextInt(spawn.spawnAmount + 1);
						mobs: for (int i = 0; i < mobs; i++) {
							int x = random.nextInt(16);
							int z = random.nextInt(16);
							if (spawn == SpawnType.NONE) {
								Block block = chunk.getBlock(x, seaLevel, z);
								if (block.getType() == Material.WATER) {
									for (Location loc : entities) {
										if (loc.distanceSquared(block.getLocation()) < spawn.minDistanceSquared) continue mobs;
									}
									spawnQueue.add(new AbstractMap.SimpleEntry<>(block.getLocation(), Zombies.DROWNED));
								}
							}else {
								int y = 1 + random.nextInt(40); // à partir de quelle hauteur ça va tenter de faire spawn
								Block prev = chunk.getBlock(x, y, z);
								y: for (; y < 140; y++) {
									boolean possible = !UNSPAWNABLE_ON.contains(prev.getType());
									prev = chunk.getBlock(x, y, z);
									if (possible && prev.getType() == Material.AIR && chunk.getBlock(x, y + 1, z).getType() == Material.AIR) {
										Location location = new Location(world, chunk.getX() << 4 | x, y, chunk.getZ() << 4 | z);
										if (OlympaZTA.getInstance().clanPlotsManager.getPlot(location) != null) continue;
										Block block = location.getBlock();
										if (block.getLightLevel() > 10 && !world.isThundering()) continue;
										for (Location loc : entities) {
											if (loc.distanceSquared(location) < spawn.minDistanceSquared) continue y;
										}
										Location lc = block.getLocation();
										spawnQueue.add(new AbstractMap.SimpleEntry<>(lc, Zombies.COMMON));
										break y;
									}
								}
							}
						}
					}
				}finally {
					queueLock.unlock();
				}
			}
		}.runTaskTimerAsynchronously(OlympaZTA.getInstance(), 40L, 102L);

		tasks[1] = new BukkitRunnable() { // s'effectue toutes les 2 secondes et demie pour spawner la moitié des mobs calculés dans la tâche 0
			public void run() {
				averageQueueSize.add(spawnQueue.size());
				if (averageQueueSize.size() > 24) averageQueueSize.poll();

				if (!queueLock.tryLock()) return;
				try {
					int i = spawnQueue.size() / 2;
					for (Iterator<Entry<Location, Zombies>> iterator = spawnQueue.iterator(); iterator.hasNext();) {
						try {
							Entry<Location, Zombies> loc = iterator.next();
							if (loc.getKey().getChunk().isLoaded()) Mobs.spawnCommonZombie(loc.getValue(), loc.getKey());
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

		enabled = true;
	}

	private Map<Chunk, SpawnType> getActiveChunks() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Chunk> processedChunks = new HashSet<Chunk>(players.size());
		Map<Chunk, SpawnType> chunks = new HashMap<>(players.size() * 8);
		for (Player p : players) {
			Location lc = p.getLocation();
			Chunk centralChunk = lc.getChunk();
			if (!processedChunks.add(centralChunk)) continue;
			if (SpawnType.getSpawnType(world, centralChunk.getX() * 16, centralChunk.getZ() * 16) == null) continue;
			if (entityCount(centralChunk) > criticalEntitiesPerChunk) continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++) {
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					Chunk chunk = world.getChunkAt(x + ax, z + az);
					if (chunks.containsKey(chunk)) continue;
					SpawnType type;
					if (world.getHighestBlockAt((x + ax) << 4, (z + az) << 4).getType() == Material.WATER) {
						type = SpawnType.NONE;
					}else type = SpawnType.getSpawnType(world, chunk.getX() * 16, chunk.getZ() * 16);
					if (type != null) {
						if (entityCount(chunk) > type.maxEntitiesPerChunk) continue;
						if (isInSafeZone(chunk)) continue;
						chunks.put(chunk, type);
					}
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

	public void addSafeZone(Region region, String id, String title) {
		OlympaCore.getInstance().getRegionManager().registerRegion(region, id, EventPriority.HIGH, new Flag().setMessages(RADAR + " vous entrez dans une " + title + "§r " + RADAR, RADAR + " vous sortez d'une " + title + "§r " + RADAR, ChatMessageType.ACTION_BAR));
		safeRegions.add(region);
		DynmapLink.showSafeArea(region, id, title);
	}

	public boolean isInSafeZone(Chunk chunk) {
		for (Region safeRegion : safeRegions) {
			if (safeRegion.isIn(chunk.getWorld(), chunk.getX() * 16, safeRegion.getMin().getBlockY(), chunk.getZ() * 16)) return true;
		}
		return false;
	}

	public double getAverageQueueSize() {
		return averageQueueSize.stream().mapToInt(Integer::intValue).average().orElse(0);
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
		return String.format("%dJ %dZ %dN %dA", players, zombies, drowned, others);
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
	
	public enum SpawnType {
		NONE(20, 1, 5, "§cerreur", null, null, null, null),
		HARD(10, 3, 15, "§c§lzone rouge", Color.RED, "Zone rouge", "Cette zone présente une forte présence en infectés.", new LootChestPicker().add(LootChestType.CIVIL, 0.5).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.4)),
		MEDIUM(12, 2, 12, "§6§lzone à risques", Color.ORANGE, "Zone à risques", "La contamination est plutôt importante dans cette zone.", new LootChestPicker().add(LootChestType.CIVIL, 0.7).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.2)),
		EASY(13, 1, 10, "§d§lzone modérée", Color.YELLOW, "Zone modérée", "Humains et zombies cohabitent, restez sur vos gardes.", new LootChestPicker().add(LootChestType.CIVIL, 0.8).add(LootChestType.CONTRABAND, 0.1).add(LootChestType.MILITARY, 0.1)),
		SAFE(22, 1, 5, "§a§lzone sécurisée", Color.LIME, "Zone sécurisée", "C'est un lieu sûr, vous pourrez croiser occasionnellement un infecté.", new LootChestPicker().add(LootChestType.CIVIL, 0.8).add(LootChestType.CONTRABAND, 0.2));
		
		private int minDistanceSquared;
		private int spawnAmount;
		private int maxEntitiesPerChunk;

		public final Color color;
		public final String name;
		public final String description;
		public final String title;

		private final LootChestPicker lootchests;

		private List<Region> regions = new ArrayList<>();
		private Flag flag;

		private SpawnType(int minDistance, int spawnAmount, int maxEntitiesPerChunk, String title, Color color, String name, String description, LootChestPicker lootchests) {
			this.minDistanceSquared = minDistance * minDistance;
			this.spawnAmount = spawnAmount;
			this.color = color;
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

		public static class SpawningFlag extends Flag {
			public final SpawnType type;

			public SpawningFlag(SpawnType type) {
				super.setMessages(RADAR + " vous entrez dans une " + type.title + "§r " + RADAR, null, ChatMessageType.ACTION_BAR);
				this.type = type;
			}

			@Override
			public boolean enters(Player p, Set<TrackedRegion> to) {
				OlympaZTA.getInstance().lineRadar.updateHolder(OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(OlympaPlayerZTA.get(p)));
				return super.enters(p, to);
			}
		}
	}

}
