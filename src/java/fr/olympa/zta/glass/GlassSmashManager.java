package fr.olympa.zta.glass;

import java.util.List;
import java.util.concurrent.TimeUnit;
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;

public class GlassSmashManager implements Listener {
	
	private Cache<Location, BlockData> blocks = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.SECONDS).removalListener((RemovalNotification<Location, BlockData> notif) -> {
		Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> notif.getKey().getBlock().setBlockData(notif.getValue()));
	}).build();
	
	private boolean enabled = true;
	
	public GlassSmashManager() {
		new GlassSmashCommand(this).register();
	}
	
	public boolean toggle() {
		if (enabled) {
			enabled = false;
			blocks.invalidateAll();
		}else enabled = true;
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
			blocks.put(location, blockData);
			location = location.clone().add(0.5, 0.5, 0.5);
			block.setType(Material.AIR);
			block.getWorld().spawnParticle(Particle.BLOCK_CRACK, location, 10, 0.1, 0.1, 0.1, blockData);
			block.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.8f, 1);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (blocks.size() == 0) return;
		List<Location> toEvict = blocks.asMap().keySet().stream().filter(x -> x.getBlockX() >> 4 == e.getChunk().getX() && x.getBlockZ() >> 4 == e.getChunk().getZ()).collect(Collectors.toList());
		if (!toEvict.isEmpty()) {
			blocks.invalidateAll(toEvict);
			OlympaZTA.getInstance().sendMessage("%d vitres replacées suite au déchargement d'un chunk.", toEvict.size());
		}
	}
	
}
