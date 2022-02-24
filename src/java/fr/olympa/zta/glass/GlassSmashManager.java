package fr.olympa.zta.glass;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;

public class GlassSmashManager implements Listener {
	
	private ReentrantLock lock = new ReentrantLock();
	private Map<Location, SimpleEntry<BlockData, Integer>> blocks = new HashMap<>();
	
	private boolean enabled = false;
	private BukkitTask task;
	
	public GlassSmashManager() {
		new GlassSmashCommand(this).register();
		
		toggle();
	}
	
	public boolean toggle() {
		if (enabled) {
			enabled = false;
			blocks.entrySet().forEach(x -> x.getKey().getBlock().setBlockData(x.getValue().getKey()));
			blocks.clear();
			task.cancel();
		}else {
			enabled = true;
			task = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
				if (lock.tryLock()) {
					for (Iterator<Entry<Location, SimpleEntry<BlockData, Integer>>> iterator = blocks.entrySet().iterator(); iterator.hasNext();) {
						Entry<Location, SimpleEntry<BlockData, Integer>> entry = iterator.next();
						if (entry.getValue().setValue(entry.getValue().getValue() - 1) == 0) {
							entry.getKey().getBlock().setBlockData(entry.getValue().getKey(), false);
							iterator.remove();
						}
					}
					lock.unlock();
				}
			}, 40L, 20L);
		}
		return enabled;
	}
	
	public boolean hit(Block block) {
		if (!enabled) return false;
		if (block == null) return false;
		Material blockType = block.getType();
		if (blockType.name().endsWith("GLASS") || blockType.name().endsWith("GLASS_PANE")) {
			Location location = block.getLocation();
			GlassSmashFlag flag = OlympaCore.getInstance().getRegionManager().getMostImportantFlag(location, GlassSmashFlag.class);
			if (flag == null || flag.isProtectedByDefault()) return false;
			BlockData blockData = block.getBlockData();
			lock.lock();
			blocks.put(location, new SimpleEntry<>(blockData, 20));
			lock.unlock();
			location = location.clone().add(0.5, 0.5, 0.5);
			block.setType(Material.AIR, false);
			block.getWorld().spawnParticle(Particle.BLOCK_CRACK, location, 10, 0.1, 0.1, 0.1, blockData);
			block.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.8f, 1);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (blocks.size() == 0) return;
		lock.lock();
		List<Location> toEvict = blocks.keySet().stream().filter(x -> x.getBlockX() >> 4 == e.getChunk().getX() && x.getBlockZ() >> 4 == e.getChunk().getZ()).collect(Collectors.toList());
		if (!toEvict.isEmpty()) {
			for (Location loc : toEvict) {
				loc.getBlock().setBlockData(blocks.remove(loc).getKey(), false);
			}
			OlympaZTA.getInstance().sendMessage("%d vitres replacées suite au déchargement d'un chunk.", toEvict.size());
		}
		lock.unlock();
	}
	
}
