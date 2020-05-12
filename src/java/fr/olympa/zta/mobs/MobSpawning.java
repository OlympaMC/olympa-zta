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
import java.util.function.Predicate;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.region.Region;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.DynmapLink;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.Entity;

public class MobSpawning {

	public static final List<Material> UNSPAWNABLE_ON = Arrays.asList(Material.AIR, Material.WATER, Material.LAVA, Material.CACTUS, Material.COBWEB);

	private BukkitTask[] tasks = new BukkitTask[2];
	private boolean enabled = false;
	private Random random = new Random();

	private LinkedList<Location> spawnQueue = new LinkedList<>();
	private Lock queueLock = new ReentrantLock();
	private Queue<Integer> averageQueueSize = new LinkedList<>();

	public World world = Bukkit.getWorlds().get(0);

	private final int chunkRadius = 2;
	private final int chunkRadiusDoubled = chunkRadius * 2;
	public int criticalEntitiesPerChunk = 25;
	public int maxEntities = 3000;

	public MobSpawning(ConfigurationSection config) {
		for (String string : config.getKeys(false)) {
			try {
				SpawnType type = SpawnType.valueOf(string);
				type.addRegions((List<Region>) config.getList(string));
			}catch (IllegalArgumentException ex) {
				OlympaZTA.getInstance().getLogger().warning("Type de spawn invalide: " + string);
			}
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
				}finally {
					queueLock.unlock();
				}
			}
		}.runTaskTimerAsynchronously(OlympaZTA.getInstance(), 40L, 102L);

		tasks[1] = new BukkitRunnable() { // s'effectue toutes les 2 secondes et demie pour spawner la moitié des mobs calculés dans la tâche 0
			public void run() {
				averageQueueSize.add(spawnQueue.size());
				if (averageQueueSize.size() > 24) averageQueueSize.remove();

				if (!queueLock.tryLock()) return;
				try {
					int i = spawnQueue.size() / 2;
					for (Iterator<Location> iterator = spawnQueue.iterator(); iterator.hasNext();) {
						try {
							Location loc = iterator.next();
							if (loc.getChunk().isLoaded()) Mobs.spawnCommonZombie(loc);
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
		}.runTaskTimer(OlympaZTA.getInstance(), 50L, 20L);

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
			if (SpawnType.getSpawnType(centralChunk) == null) continue;
			if (entityCount(centralChunk) > criticalEntitiesPerChunk) continue;
			int x = lc.getBlockX() / 16 - chunkRadius;
			int z = lc.getBlockZ() / 16 - chunkRadius;
			for (int ax = 0; ax <= chunkRadiusDoubled; ax++) {
				for (int az = 0; az <= chunkRadiusDoubled; az++) {
					Chunk chunk = world.getChunkAt(x + ax, z + az);
					if (chunks.containsKey(chunk)) continue;
					SpawnType type = SpawnType.getSpawnType(chunk);
					if (type != null) {
						if (entityCount(chunk) > type.maxEntitiesPerChunk) continue;
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
	
	public enum SpawnType {
		HARD(10, 3, 20, "§c§lzone rouge", Color.RED, "Zone rouge", "Cette zone présente une forte présence en infectés."),
		MEDIUM(12, 2, 15, "§6§lzone à risques", Color.ORANGE, "Zone à risques", "La contamination est plutôt importante dans cette zone."),
		EASY(13, 1, 12, "§d§lzone modérée", Color.YELLOW, "Zone modérée", "Humains et zombies cohabitent, restez sur vos gardes."),
		SAFE(22, 1, 5, "§a§lzone sécurisée", Color.LIME, "Zone sécurisée", "C'est un lieu sûr, vous pourrez croiser occasionnellement un infecté.");
		
		private List<Region> regions = new ArrayList<>();
		private int minDistanceSquared;
		private int spawnAmount;
		private int maxEntitiesPerChunk;
		
		private Predicate<Player> enterPredicate;
		private BaseComponent[] texts;

		public final Color color;
		public final String name;
		public final String description;
		public final String title;

		private SpawnType(int minDistance, int spawnAmount, int maxEntitiesPerChunk, String title, Color color, String name, String description) {
			this.minDistanceSquared = minDistance ^ 2;
			this.spawnAmount = spawnAmount;
			this.color = color;
			this.name = name;
			this.description = description;
			this.title = title;

			texts = TextComponent.fromLegacyText("§8§k§lgdn§r§7 vous entrez dans une " + title + "§r §8§k§lndg");

			enterPredicate = player -> {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, texts);
				return false;
			};
		}

		public boolean isInto(Chunk chunk) {
			for (Region region : regions) {
				if (region.isIn(chunk.getWorld(), chunk.getX() * 16, region.getMin().getBlockY(), chunk.getZ() * 16)) return true;
			}
			return false;
		}

		public void addRegions(Collection<Region> regions) {
			for (Region region : regions) {
				OlympaCore.getInstance().getRegionManager().registerRegion(region, name() + this.regions.size(), enterPredicate, null);
				this.regions.add(region);
				DynmapLink.showArea(region, this);
			}
		}

		public List<Region> getRegions() {
			return regions;
		}

		public static SpawnType getSpawnType(Chunk chunk) {
			for (SpawnType type : values()) {
				if (type.isInto(chunk)) return type;
			}
			return null;
		}
	}

}
