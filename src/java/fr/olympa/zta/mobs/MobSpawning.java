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
import org.bukkit.GameMode;
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

import fr.olympa.api.spigot.region.Point2D;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.RegionEventReason;
import fr.olympa.api.spigot.region.tracking.flags.Flag;
import fr.olympa.api.spigot.utils.CustomDayDuration;
import fr.olympa.api.utils.RandomizedPickerBase;
import fr.olympa.api.utils.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.utils.RandomizedPickerBase.Conditioned;
import fr.olympa.api.utils.RandomizedPickerBase.IConditionalBuilder;
import fr.olympa.api.utils.RandomizedPickerBase.RandomizedPicker;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.glass.GlassSmashFlag;
import fr.olympa.zta.loot.chests.type.LootChestType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.utils.map.DynmapLink;
import fr.olympa.zta.utils.map.DynmapZoneConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.minecraft.server.v1_16_R3.Entity;

public class MobSpawning implements Runnable {

	public static final IConditionalBuilder<Zombies, MobSpawningContext> DEFAULT_ZOMBIE_PICKER = RandomizedPickerBase.<Zombies>newBuilder()
			.add(1, Zombies.COMMON)
			.add(0.14, new TimeConditioned(Zombies.SPEED, CustomDayDuration.NIGHT_TIME))
			.add(0.09, new TimeConditioned(Zombies.TANK, CustomDayDuration.NIGHT_TIME));
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
		for (String type : spawnRegions.getKeys(false))
			try {
				SpawnType.valueOf(type).addRegions((List<Region>) spawnRegions.getList(type));
			} catch (IllegalArgumentException ex) {
				OlympaZTA.getInstance().getLogger().warning("Type de spawn invalide: " + type);
			}

		for (String id : safeRegions.getKeys(false))
			addSafeZone(safeRegions.getSerializable(id + ".region", Region.class), id, safeRegions.getString(id + ".title"));
	}

	@Override
	public void run() {
		while (true) {
			long time = System.currentTimeMillis();
			queueLock.lock();
			try {
				List<Location> entities = world.getLivingEntities()./*getPlayers().*/stream().map(LivingEntity::getLocation).collect(Collectors.toList());
				if (entities.size() > maxEntities)
					return;
				long time2 = System.currentTimeMillis();
				Map<ChunkSnapshot, SpawnType> activeChunks = getActiveChunks();
				timeActiveChunks = System.currentTimeMillis() - time2;
				lastActiveChunks = activeChunks.size();
				for (Entry<ChunkSnapshot, SpawnType> entry : activeChunks.entrySet()) { // itère dans tous les chunks actifs
					ChunkSnapshot chunk = entry.getKey();
					SpawnType spawn = entry.getValue();
					int attempts = 0;
					int mobs = spawn.spawning.spawnAmount();
					/*if (mobs > 1) */mobs = random.nextInt(mobs + 1);
					if (world.getTime() > CustomDayDuration.NIGHT_TIME) mobs++;
					mobs: for (int i = 0; i < mobs; i++) { // boucle pour faire spawner un nombre de mobs aléatoires
						if (++attempts == 5)
							break;
						int x = random.nextInt(16);
						int z = random.nextInt(16); // random position dans le chunk
						if (spawn == SpawnType.NONE) { // none = chunk océan, tenter de faire spawn un noyé
							Material block = chunk.getBlockType(x, seaLevel, z);
							if (block == Material.WATER) { // si le bloc au niveau de l'océan est de l'eau, spawner
								Location location = new Location(world, chunk.getX() << 4 | x, seaLevel, chunk.getZ() << 4 | z);
								for (Location entityLocation : entities)
									if (entityLocation.distanceSquared(location) < spawn.minDistanceSquared)
										continue mobs; // trop proche d'entité = abandon
								spawnQueue.add(new AbstractMap.SimpleEntry<>(location, Zombies.DROWNED));
							}
						} else {
							int highestY = chunk.getHighestBlockYAt(x, z);
							int y = random.nextInt(Math.min(highestY - 1, 40)) + 1; // à partir de quelle hauteur ça va tenter de faire spawn
							Material prev = chunk.getBlockType(x, y - 1, z);
							y: for (; y < highestY + 1; y++) { // loop depuis l'hauteur aléatoire jusqu'à 140 (pas de spawn au dessus)
								boolean possible = !UNSPAWNABLE_ON.contains(prev);
								prev = chunk.getBlockType(x, y, z);
								if (possible && prev == Material.AIR && chunk.getBlockType(x, y + 1, z) == Material.AIR) { // si bloc possible en dessous ET air au bloc ET air au-dessus = good
									if (chunk.getBlockSkyLight(x, y, z) <= 5 || chunk.getBlockEmittedLight(x, y, z) > 10) {
										if (random.nextBoolean())
											continue; // au fond d'un immeuble pas éclairé : pas intéressant
										mobs++;
										continue mobs;
									}
									Location location = new Location(world, chunk.getX() << 4 | x, y, chunk.getZ() << 4 | z);
									SpawningFlag flag = OlympaCore.getInstance().getRegionManager().getMostImportantFlag(location, SpawningFlag.class);
									if (flag == null || flag.type == null)
										continue;
									if (OlympaZTA.getInstance().clanPlotsManager.getPlot(location) != null)
										continue; // si on est dans une parcelle de clan pas de spawn
									for (Location loc : entities)
										if (loc.distanceSquared(location) < spawn.minDistanceSquared)
											continue y; // trop près d'autre entité
									for (int j = 0; j < spawn.spawning.spawnAmount(); j++)
										spawnQueue.add(new AbstractMap.SimpleEntry<>(location, spawn.spawning.getZombiePicker().pickOne(random, new MobSpawningContext())));
									continue mobs;
								}
							}
						}
					}
				}
			}catch (Exception ex) {
				OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du spawn de mobs.");
				ex.printStackTrace();
				end();
			} finally {
				queueLock.unlock();
				if (!enabled)
					break;
				long elapsed = System.currentTimeMillis() - time;
				computeTimes.add(elapsed);
				try {
					if (elapsed < 5000)
						Thread.sleep(5000 - elapsed);
				} catch (InterruptedException e) {
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
			@Override
			public void run() {
				queueSize.add(spawnQueue.size());

				if (!queueLock.tryLock())
					return;
				try {
					lastSpawnedMobs = 0;
					int i = spawnQueue.size() / 2;
					for (Iterator<Entry<Location, Zombies>> iterator = spawnQueue.iterator(); iterator.hasNext();) {
						try {
							Entry<Location, Zombies> loc = iterator.next();
							if (loc.getKey().isChunkLoaded()) {
								Mobs.spawnCommonZombie(loc.getValue(), loc.getKey());
								lastSpawnedMobs++;
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						iterator.remove();
						if (--i == 0)
							return;
					}
				} finally {
					queueLock.unlock();
				}
			}
		}.runTaskTimer(OlympaZTA.getInstance(), 20L, 50L);
		
		OlympaZTA.getInstance().sendMessage("Le spawn de mobs a démarré.");
		return true;
	}

	private Map<ChunkSnapshot, SpawnType> getActiveChunks() {
		List<Player> players = Bukkit.getOnlinePlayers().stream().filter(x -> x.getGameMode() != GameMode.SPECTATOR).collect(Collectors.toList());
		Set<Chunk> processedChunks = new HashSet<>(players.size() + 1, 1);
		List<Point2D> points = new ArrayList<>(players.size() * 8);
		Map<ChunkSnapshot, SpawnType> chunks = new HashMap<>(players.size() * 8, 1);
		for (Player p : players) {
			Location lc = p.getLocation();
			Chunk centralChunk = lc.getChunk();
			if (!processedChunks.add(centralChunk))
				continue; // chunk déjà calculé

			if (centralChunk.getBlock(0, seaLevel, 0).getType() != Material.WATER && SpawnType.getSpawnType(centralChunk) == null)
				continue;

			if (entityCount(centralChunk) > criticalEntitiesPerChunk)
				continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++)
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					if (!world.isChunkLoaded(x + ax, z + az)) continue;
					Chunk chunk = world.getChunkAt(x + ax, z + az);
					ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
					Point2D point = new Point2D(chunk);
					if (points.contains(point))
						continue;
					SpawnType type;
					if (snapshot.getBlockType(0, seaLevel, 0) == Material.WATER)
						type = SpawnType.NONE;
					else
						type = SpawnType.getSpawnType(chunk);
					if (type != null) {
						if (entityCount(chunk) > type.spawning.maxEntitiesPerChunk())
							continue;
						if (isInSafeZone(chunk))
							continue;
						chunks.put(snapshot, type);
						points.add(point);
					}
				}
		}
		return chunks;
	}

	private int entityCount(Chunk chunk) {
		int count = 0;
		net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle(); // + opti de passer par les NMS, sinon Bukkit construit une array avec les bukkit entity et c'est lourd
		for (List<Entity> slice : nmsChunk.entitySlices)
			count += slice.size();
		return count;
	}

	public void addSafeZone(Region region, String id, String title) {
		OlympaCore.getInstance().getRegionManager().registerRegion(region, id, EventPriority.HIGH,
				new Flag().setMessages(RADAR + " vous entrez dans une " + title + "§r " + RADAR, RADAR + " vous sortez d'une " + title + "§r " + RADAR, ChatMessageType.ACTION_BAR), new SpawnType.SpawningFlag(null, false));
		safeRegions.add(region);
		DynmapLink.showSafeArea(region, id, title);
	}

	public boolean isInSafeZone(Chunk chunk) {
		for (Region safeRegion : safeRegions)
			if (safeRegion.isIn(chunk.getWorld(), chunk.getX() * 16, safeRegion.getMin().getBlockY(), chunk.getZ() * 16))
				return true;
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
		for (LivingEntity entity : world.getLivingEntities())
			if (entity instanceof Drowned)
				drowned++;
			else if (entity instanceof Zombie)
				zombies++;
			else if (entity instanceof Player)
				players++;
			else
				others++;
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
		
		OlympaZTA.getInstance().sendMessage("Le spawn de mobs est désormais inactif.");
	}

	public enum SpawnType {
		NONE(
				new MobSpawningConfig(12, 1, 5, null),
				false,
				"§c§lerreur",
				"§cerreur",
				null,
				null),
		HARD(
				new MobSpawningConfig(10, 2, 6, DEFAULT_ZOMBIE_PICKER.clone().add(0.1, Zombies.TNT).add(0.01, Zombies.SPEED).add(0.002, Zombies.TANK).build()),
				true,
				"§c§lzone rouge",
				"§7§ogare au zombies!",
				new DynmapZoneConfig(Color.RED, "621100", "Zone rouge", "Cette zone présente une forte présence en infectés."),
				RandomizedPickerBase.<LootChestType>newBuilder().add(0.5, LootChestType.CIVIL).add(0.1, LootChestType.CONTRABAND).add(0.4, LootChestType.MILITARY).build()),
		MEDIUM(
				new MobSpawningConfig(12, 2, 5, DEFAULT_ZOMBIE_PICKER.clone().add(0.08, Zombies.TNT).add(0.005, Zombies.SPEED).build()),
				true,
				"§6§lzone à risques",
				"§7§osoyez sur vos gardes",
				new DynmapZoneConfig(Color.ORANGE, "984C00", "Zone à risques", "La contamination est plutôt importante dans cette zone."),
				RandomizedPickerBase.<LootChestType>newBuilder().add(0.7, LootChestType.CIVIL).add(0.1, LootChestType.CONTRABAND).add(0.2, LootChestType.MILITARY).build()),
		EASY(
				new MobSpawningConfig(15, 1, 4, DEFAULT_ZOMBIE_PICKER.clone().add(0.012, Zombies.TNT).build()),
				true,
				"§d§lzone modérée",
				"§7§ogardez vos distances",
				new DynmapZoneConfig(Color.YELLOW, "8B7700", "Zone modérée", "Humains et zombies cohabitent, restez sur vos gardes."),
				RandomizedPickerBase.<LootChestType>newBuilder().add(0.8, LootChestType.CIVIL).add(0.1, LootChestType.CONTRABAND).add(0.1, LootChestType.MILITARY).build()),
		SAFE(
				new MobSpawningConfig(21, 1, 2, DEFAULT_ZOMBIE_PICKER.clone().add(0.008, Zombies.TNT).build()),
				false,
				"§a§lzone sécurisée",
				"§7§orestez vigilant",
				new DynmapZoneConfig(Color.LIME, "668B00", "Zone sécurisée", "C'est un lieu sûr, vous pourrez croiser occasionnellement un infecté."),
				RandomizedPickerBase.<LootChestType>newBuilder().add(0.8, LootChestType.CIVIL).add(0.2, LootChestType.CONTRABAND).build());

		private static Map<Chunk, SpawnType> chunks = new HashMap<>();

		public final MobSpawningConfig spawning;
		public final int minDistanceSquared;

		public final String title;
		public final String subtitle;

		public final DynmapZoneConfig dynmap;

		private final RandomizedPicker<LootChestType> lootchests;

		private List<Region> regions = new ArrayList<>();
		private Flag flag;

		SpawnType(MobSpawningConfig spawning, boolean glassSmash, String title, String subtitle, DynmapZoneConfig dynmap, RandomizedPicker<LootChestType> lootchests) {
			if (spawning.getZombiePicker() != null) System.out.println(name() + " DEFAULT " + DEFAULT_ZOMBIE_PICKER.build().getConditionedObjectsList().size() + " SPAWNING " + spawning.getZombiePicker().getConditionedObjectsList().size());
			this.spawning = spawning;
			this.title = title;
			this.subtitle = subtitle;
			this.dynmap = dynmap;
			this.lootchests = lootchests;

			minDistanceSquared = spawning.minDistance() * spawning.minDistance();

			flag = new SpawningFlag(this, glassSmash);
		}

		public RandomizedPicker<LootChestType> getLootChests() {
			return lootchests;
		}

		public boolean isInto(World world, int x, int z) {
			for (Region region : regions)
				if (region.isIn(world, x, region.getMin().getBlockY(), z))
					return true;
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
			for (SpawnType type : values())
				if (type.isInto(world, x, z))
					return type;
			return null;
		}

		public static SpawnType getSpawnType(Chunk chunk) {
			if (chunks.containsKey(chunk))
				return chunks.get(chunk);
			SpawnType type = getSpawnType(chunk.getWorld(), chunk.getX() * 16, chunk.getZ() * 16);
			chunks.put(chunk, type);
			return type;
		}

		public static class SpawningFlag extends GlassSmashFlag {
			public final SpawnType type;

			public SpawningFlag(SpawnType type, boolean glassSmash) {
				super(!glassSmash);
				this.type = type;
				if (type != null)
					super.setMessages(RADAR + " vous entrez dans une " + type.title + "§r " + RADAR, null, ChatMessageType.ACTION_BAR);
			}

			@Override
			public ActionResult enters(EntryEvent event) {
				if (type != null && event.getReason() != RegionEventReason.JOIN) {
					OlympaPlayerZTA player = OlympaPlayerZTA.get(event.getPlayer());
					if (player.parameterZoneTitle.get())
						event.getPlayer().sendTitle(type.title, type.subtitle, 7, 43, 10);
				}
				return super.enters(event);
			}
		}
	}

	public static class TimeConditioned implements Conditioned<Zombies, MobSpawningContext> {

		private Zombies zombie;
		private int minTime;
		private int maxTime;

		public TimeConditioned(Zombies zombie, int minTime) {
			this(zombie, minTime, 24000);
		}

		public TimeConditioned(Zombies zombie, int minTime, int maxTime) {
			this.zombie = zombie;
			this.minTime = minTime;
			this.maxTime = maxTime;
		}

		@Override
		public Zombies getObject() {
			return zombie;
		}

		@Override
		public boolean isValidWithNoContext() {
			return true;
		}

		@Override
		public boolean isValid(MobSpawningContext context) {
			long time = OlympaZTA.getInstance().mobSpawning.world.getTime();
			return minTime < time && time < maxTime;
		}
		
		@Override
		public int hashCode() {
			int hash = 11 * zombie.ordinal();
			hash += 7 * minTime;
			hash += 7 * maxTime;
			return hash;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof TimeConditioned other && other.zombie == zombie && other.minTime == minTime && other.maxTime == maxTime;
		}

	}
	
	public static class MobSpawningContext extends ConditionalContext {
		
	}

}
