package fr.olympa.zta.weapons.guns.ambiance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.utils.RandomizedPicker.Chanced;
import fr.olympa.api.utils.RandomizedPicker.FixedPicker;
import fr.olympa.zta.OlympaZTA;

public class SoundAmbiance implements Runnable {
	
	private BukkitTask task;
	private FixedPicker<Scenario> scenarioPicker = new FixedPicker<>(1, 1, 0, Scenario.values());
	private List<AmbianceScenario> scenarios = new ArrayList<>();
	
	@Override
	public void run() {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		if (players.isEmpty()) return;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (Iterator<AmbianceScenario> iterator = scenarios.iterator(); iterator.hasNext();) {
			AmbianceScenario scenario = iterator.next();
			if (scenario.shouldTerminate()) {
				iterator.remove();
			}else {
				players.forEach(scenario::run);
			}
		}
		if (scenarios.size() < 2 && random.nextDouble() < 0.05) {
			float volume = (float) random.nextDouble(0.03, 0.12);
			float pitch = (float) random.nextDouble(0.7 + volume, 0.9);
			AmbianceScenario scenario = scenarioPicker.pick(random).get(0).creator.apply(volume, pitch);
			scenarios.add(scenario);
		}
	}
	
	public void start() {
		if (task != null) return;
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), this, 30, 1);
	}
	
	private enum Scenario implements Chanced {
		SILENT(50, SilentScenario::new),
		AUTO(20, AutoScenario::new),
		SIMPLE(20, SimpleScenario::new),
		EXPLOSION(10, ExplosionScenario::new),
		;
		
		private int chance;
		private BiFunction<Float, Float, AmbianceScenario> creator;
		
		private Scenario(int chance, BiFunction<Float, Float, AmbianceScenario> creator) {
			this.chance = chance;
			this.creator = creator;
		}
		
		@Override
		public double getChance() {
			return chance;
		}
		
	}
	
	public enum ZTASound {
		GUN_PUMP("zta.guns.pump"),
		GUN_AUTO("zta.guns.auto", "zta.guns.auto_far"),
		GUN_BARRETT("zta.guns.barrett"),
		GUN_GENERIC("zta.guns.generic", "zta.guns.generic_far"),
		EXPLOSION_CIVIL("zta.explosions.civil"),
		;
	
		private final String sound, farSound;
		
		private ZTASound(String sound) {
			this(sound, sound);
		}
		
		private ZTASound(String sound, String farSound) {
			this.sound = sound;
			this.farSound = farSound;
		}
		
		public String getSound() {
			return sound;
		}
		
		public String getFarSound() {
			return farSound;
		}
		
		public static ZTASound getRandom() {
			return ZTASound.values()[ThreadLocalRandom.current().nextInt(ZTASound.values().length)];
		}
		
	}
	
	public void stop() {
		if (task == null) return;
		task.cancel();
		task = null;
	}
	
}
