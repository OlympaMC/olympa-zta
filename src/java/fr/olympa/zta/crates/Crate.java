package fr.olympa.zta.crates;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.BlinkingLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.lootchests.RandomizedInventory;
import fr.olympa.zta.lootchests.creators.LootCreator;

public class Crate extends RandomizedInventory {
	
	private static final BlinkingLine<HologramLine> LINE = new BlinkingLine<>((color, x) -> color + "§lCaisse d'équipement", OlympaZTA.getInstance(), 50, ChatColor.RED, ChatColor.DARK_RED);
	
	private Location location;
	private CrateType type;
	
	protected Block block;
	private int diff = 50;
	private BukkitTask task, unloadTask;
	private Hologram hologram;
	private DynamicLine<HologramLine> timerLine = new DynamicLine<>(x -> diff > 0 ? "§7En approche... " + diff : "§7§nDébloquée !");
	
	public Crate(Location location, CrateType type) {
		super("Caisse d'équipement", 3);
		this.location = location;
		this.type = type;
	}
	
	public void startFalling() {
		Validate.isTrue(block == null, "Crate already started falling.");
		location.getChunk().addPluginChunkTicket(OlympaZTA.getInstance());
		int highestY = location.getWorld().getHighestBlockYAt(location);
		location.setY(Math.min(highestY + 35, location.getWorld().getMaxHeight()));
		block = location.getBlock();
		location = block.getLocation(); // pour obtenir la location sur le coin du bloc
		location.setY(highestY + 1);
		location.add(0.5, 0, 0.5);
		task = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
			block.setType(Material.AIR);
			block = block.getRelative(BlockFace.DOWN);
			block.setType(Material.TRAPPED_CHEST);
			diff = block.getY() - highestY - 1;
			timerLine.updateGlobal();
			if (block.getRelative(BlockFace.DOWN).getType() != Material.AIR) { // touché le sol
				task.cancel();
				task = null;
				unloadTask = Bukkit.getScheduler().runTaskLater(OlympaZTA.getInstance(), () -> OlympaZTA.getInstance().crates.unloadCrate(this), 5 * 60 * 20);
				location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.5f, 0.1f);
			}
			if (diff < 20) {
				location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 30 - diff, 0.8, 0, 0.8, 0.01);
				if (diff < 15) location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 15 - diff, 0.8, 0, 0.8, 0.01);
			}
			if (diff < 20) {
				location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, (20f - diff) / 15f, 1);
			}
		}, 0, 20);
		hologram = OlympaCore.getInstance().getHologramsManager().createHologram(location.clone().add(0, 1, 0), false, true, LINE, timerLine);
	}
	
	public void cancel() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		if (unloadTask != null) {
			location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 1, 1);
			unloadTask.cancel();
			unloadTask = null;
		}
		if (block != null) block.setType(Material.AIR);
		if (hologram != null) hologram.remove();
		location.getChunk().removePluginChunkTicket(OlympaZTA.getInstance());
	}
	
	public void click(Player p) {
		if (diff == 0) {
			Prefix.DEFAULT_BAD.sendMessage(p, "La caisse n'a pas encore aterri !");
		}else {
			super.create(p);
		}
	}
	
	@Override
	public int getMinItems() {
		return 3;
	}
	
	@Override
	public int getMaxItems() {
		return 7;
	}
	
	@Override
	public List<LootCreator> getObjectList() {
		return type.getCreatorsSimple();
	}
	
	@Override
	public List<LootCreator> getAlwaysObjectList() {
		return type.getCreatorsAlways();
	}
	
}
