package fr.olympa.zta.weapons.guns.ambiance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.randomized.RandomValueProvider;
import fr.olympa.api.common.randomized.RandomizedPickerBase.FixedMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedMultiPicker;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;

public class SoundAmbiance implements Runnable {

	private final double[] chances = { 0.06, 0.005 };

	private BukkitTask task;
	private RandomizedMultiPicker<Scenario> scenarioPicker = new FixedMultiPicker<>(Arrays.stream(Scenario.values()).collect(Collectors.toMap(x -> x, Scenario::getChance)), Collections.emptyList(), new RandomValueProvider.UniformProvider(1, 1), 0);
	private List<AmbianceScenario> scenarios = new ArrayList<>();

	@Override
	public void run() {
		Collection<? extends Player> players = OlympaZTA.getInstance().mobSpawning.world.getPlayers();
		if (players.isEmpty())
			return;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (Iterator<AmbianceScenario> iterator = scenarios.iterator(); iterator.hasNext();) {
			AmbianceScenario scenario = iterator.next();
			if (scenario.shouldTerminate())
				iterator.remove();
			else
				players.stream().filter(x -> OlympaPlayerZTA.get(x).parameterAmbient.get()).forEach(scenario::run);
		}
		if (scenarios.size() == chances.length)
			return;
		if (random.nextDouble() < chances[scenarios.size()]) {
			float volume = (float) random.nextDouble(0.009, 0.05);
			float pitch = (float) random.nextDouble(0.7 + volume, 0.9);
			AmbianceScenario scenario = scenarioPicker.pickOne(random).creator.apply(volume, pitch);
			scenarios.add(scenario);
		}
	}

	public void start() {
		if (task != null)
			return;
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), this, 30, 1);
	}

	private enum Scenario {
		SILENT(45, SilentScenario::new),
		AUTO(11, AutoScenario::new),
		SIMPLE(15, SimpleScenario::new),
		EXPLOSION(10, ExplosionScenario::new),
		;

		private int chance;
		private BiFunction<Float, Float, AmbianceScenario> creator;

		Scenario(int chance, BiFunction<Float, Float, AmbianceScenario> creator) {
			this.chance = chance;
			this.creator = creator;
		}

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
		ZOMBIE_AMBIENT("entity.zombie.ambient"),
		HELICO_LANDING("zta.quests.helicopter_landing");

		private final String sound, farSound;

		ZTASound(String sound) {
			this(sound, sound);
		}

		ZTASound(String sound, String farSound) {
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
		if (task == null)
			return;
		task.cancel();
		task = null;
	}

}
