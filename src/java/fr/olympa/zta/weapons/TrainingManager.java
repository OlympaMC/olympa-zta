package fr.olympa.zta.weapons;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.DropFlag;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.weapons.guns.GunFlag;

public class TrainingManager implements Listener {
	
	private Location enterButton;
	private List<Location> exitButtons;
	private List<TrainingSlot> slots;
	private Location exitLocation;
	private Region playersRegion;
	private Region mobsRegion;
	
	private BukkitTask mobsSpawn;
	
	private DynamicLine<HologramLine> slotsLine = new DynamicLine<>(holo -> {
		long available = slots.stream().filter(TrainingSlot::isEmpty).count();
		return "§e§l" + available + "/" + slots.size() + "§e places disponibles";
	});
	
	public TrainingManager(ConfigurationSection config) {
		this.enterButton = SpigotUtils.convertStringToLocation(config.getString("enterButton"));
		this.exitButtons = config.getStringList("exitButtons").stream().map(SpigotUtils::convertStringToLocation).collect(Collectors.toList());
		this.slots = config.getStringList("locations").stream().map(loc -> new TrainingSlot(SpigotUtils.convertStringToLocation(loc))).collect(Collectors.toList());
		this.exitLocation = SpigotUtils.convertStringToLocation(config.getString("exitLocation"));
		this.playersRegion = config.getSerializable("playersRegion", Region.class);
		this.mobsRegion = config.getSerializable("mobsRegion", Region.class);
		
		OlympaCore.getInstance().getRegionManager().registerRegion(playersRegion, "training_players", EventPriority.HIGH, new GunFlag(false, true), new DropFlag(true), new Flag() {
			@Override
			public ActionResult leaves(Player p, Set<TrackedRegion> to) {
				TrainingSlot slot = getTrainingSlot(p);
				if (slot != null) slot.exit(false);
				return super.leaves(p, to);
			}
		});
		OlympaCore.getInstance().getHologramsManager().createHologram(enterButton.clone().add(0.5, 1, 0.5), false, new CyclingLine<>(Arrays.asList("§6§lSTAND DE TIR", "§e§lSTAND DE TIR"), 40), slotsLine, FixedLine.EMPTY_LINE, new FixedLine<>("§aCliquez sur le bouton"), new FixedLine<>("§apour rejoindre le stand."), new FixedLine<>("§7(munitions données)"));
		
		mobsSpawn = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
			if (slots.stream().allMatch(TrainingSlot::isEmpty)) return;
			Location random = mobsRegion.getRandomLocation();
			if (!random.getBlock().isEmpty()) return;
			if (random.getWorld().getNearbyEntities(random, 50, 50, 50, entity -> entity.getType() == EntityType.ZOMBIE).size() > 10) return;
			Mobs.spawnCommonZombie(Zombies.TRAINING, random);
		}, 80, 50);
	}
	
	private TrainingSlot getTrainingSlot(Player player) {
		for (TrainingSlot slot : slots) {
			if (player.equals(slot.player)) return slot;
		}
		return null;
	}
	
	private void tryEnter(Player p) {
		for (TrainingSlot slot : slots) {
			if (slot.player == null) {
				slot.enter(p);
				return;
			}
		}
		Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a plus de place disponible dans le stand de tir... Revenez plus tard !");
	}
	
	public void unload() {
		for (TrainingSlot slot : slots) {
			if (!slot.isEmpty()) slot.exit(true);
		}
		mobsSpawn.cancel();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) return;
		Location location = e.getClickedBlock().getLocation();
		if (location.equals(enterButton)) {
			tryEnter(e.getPlayer());
		}else if (exitButtons.contains(location)) {
			TrainingSlot slot = getTrainingSlot(e.getPlayer());
			if (slot != null) slot.exit(true);
		}
	}
	
	class TrainingSlot {
		final static int MINUTES = 2;
		final static double SECONDS = MINUTES * 60;
		
		final Location location;
		
		Player player;
		BukkitTask ejection;
		
		BossBar bar;
		int timeElapsed;
		
		public TrainingSlot(Location location) {
			this.location = location;
		}
		
		public boolean isEmpty() {
			return player == null;
		}
		
		public void enter(Player player) {
			this.player = player;
			Prefix.DEFAULT_GOOD.sendMessage(player, "Bienvenue dans le stand de tir ! Vous êtes là pour une durée de %d minutes maximum.", MINUTES);
			
			timeElapsed = 0;
			bar = Bukkit.createBossBar("§aStand de tir", BarColor.GREEN, BarStyle.SEGMENTED_12);
			bar.setProgress(0);
			bar.addPlayer(player);
			player.teleport(location);
			ejection = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), (Runnable) () -> {
				if (timeElapsed >= SECONDS) {
					exit(true);
					return;
				}
				timeElapsed++;
				bar.setProgress(timeElapsed / SECONDS);
			}, 20L, 20L);
			slotsLine.updateGlobal();
		}
		
		public void exit(boolean teleport) {
			if (ejection != null) {
				ejection.cancel();
				ejection = null;
				bar.removeAll();
				bar = null;
			}
			Player tmp = player;
			player = null;
			Prefix.DEFAULT.sendMessage(tmp, "Vous quittez le stand de tir.");
			slotsLine.updateGlobal();
			if (teleport) tmp.teleport(exitLocation);
		}
	}
	
}
